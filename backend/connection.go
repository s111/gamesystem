package main

import (
	"log"
	"net/http"
	"time"

	"github.com/gorilla/websocket"
)

const (
	writeWait      = 10 * time.Second
	pongWait       = 60 * time.Second
	pingPeriod     = (pongWait * 9) / 10
	maxMessageSize = 512
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
}

type connection struct {
	ws       *websocket.Conn
	sendList chan []Game
}

type Message struct {
	Action string `json:"action"`
	Data   string `json:"data"`
}

func (c *connection) listenRead() {
	defer func() {
		c.ws.Close()
	}()

	c.ws.SetReadLimit(maxMessageSize)
	c.ws.SetReadDeadline(time.Now().Add(pongWait))
	c.ws.SetPongHandler(func(string) error {
		c.ws.SetReadDeadline(time.Now().Add(pongWait))

		return nil
	})

	for {
		msg := &Message{}
		err := c.ws.ReadJSON(msg)

		log.Println(msg)

		if msg.Action == "select" {
			scheduler.start(msg.Data)
		} else if msg.Action == "ready" {
			// broadcast ready to all clients but this
		}

		if err != nil {
			break
		}
	}
}

func (c *connection) writeMessage(mt int, payload []byte) error {
	c.ws.SetWriteDeadline(time.Now().Add(writeWait))

	return c.ws.WriteMessage(mt, payload)
}

func (c *connection) writeJSON(payload interface{}) error {
	c.ws.SetWriteDeadline(time.Now().Add(writeWait))

	return c.ws.WriteJSON(payload)
}

func (c *connection) listenWrite() {
	ticker := time.NewTicker(pingPeriod)

	defer func() {
		ticker.Stop()
		c.ws.Close()
	}()

	for {
		select {
		case list, ok := <-c.sendList:
			if !ok {
				c.writeMessage(websocket.CloseMessage, []byte{})

				return
			}

			if err := c.writeJSON(list); err != nil {
				return
			}
		case <-ticker.C:
			if err := c.writeMessage(websocket.PingMessage, []byte{}); err != nil {
				return
			}
		}
	}
}

func serverWs(w http.ResponseWriter, r *http.Request) {
	if r.Method != "GET" {
		http.Error(w, "Method not allowed", 405)

		return
	}

	ws, err := upgrader.Upgrade(w, r, nil)

	if err != nil {
		log.Println(err)

		return
	}

	c := &connection{
		sendList: make(chan []Game),
		ws:       ws,
	}

	go c.listenWrite()

	var games []Game

	for _, game := range scheduler.games {
		games = append(games, game)
	}

	c.sendList <- games
	c.listenRead()
}
