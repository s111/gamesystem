package main

import (
	"encoding/json"
	"errors"
	"log"
	"net/http"
	"time"

	"github.com/gorilla/websocket"
)

const (
	writeWait      = 10 * time.Second
	pongWait       = 10 * time.Second
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
	id   string
	ws   *websocket.Conn
	send chan messageOut
}

type messageIn struct {
	Action string          `json:"action"`
	Data   json.RawMessage `json:"data"`
}

type messageOut struct {
	Action string      `json:"action"`
	Data   interface{} `json:"data"`
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
		msg := &messageIn{}
		err := c.ws.ReadJSON(msg)

		if err != nil {
			if c.id != "" {
				err = errors.New(err.Error() + " (" + c.id + ")")
			}

			log.Println("Dropping client:", err)

			return
		}

		log.Println("Recieved message:", msg)

		switch msg.Action {
		case "identify":
			err := json.Unmarshal(msg.Data, &c.id)

			if err != nil {
				log.Println("Dropping client:", err)
			}

			h.register <- c
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
		case m, ok := <-c.send:
			if !ok {
				c.writeMessage(websocket.CloseMessage, []byte{})

				return
			}

			if err := c.writeJSON(m); err != nil {
				log.Println("Dropping client:", err)

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
		log.Println("Dropping client:", err)

		return
	}

	c := &connection{
		ws:   ws,
		send: make(chan messageOut),
	}

	go c.listenWrite()

	c.send <- messageOut{Action: "identify"}
	c.listenRead()

	m := messageOut{
		Action: ActionDrop,
		Data:   c.id,
	}

	h.sendToGame <- m
}
