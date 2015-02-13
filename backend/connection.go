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

type request struct {
	id   string
	conn chan *connection
	err  chan error
}

type connection struct {
	id      string
	active  bool
	timeout *time.Timer
	ws      *websocket.Conn
	send    chan messageOut
}

type messageIn struct {
	To     string          `json:"to,omitempty"`
	Action string          `json:"action,omitempty"`
	Data   json.RawMessage `json:"data,omitempty"`
}

type messageOut struct {
	To     string           `json:"to,omitempty"`
	From   string           `json:"from,omitempty"`
	Action string           `json:"action,omitempty"`
	Data   interface{}      `json:"data,omitempty"`
	Raw    *json.RawMessage `json:"raw,omitempty"`
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
		case ActionPassthrough:
			h.passthrough(msg.To, c.id, &msg.Data)
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

func getId(ws *websocket.Conn) (string, error) {
	var id string

	ws.WriteJSON(messageOut{Action: ActionIdentify})

	msg := &messageIn{}
	err := ws.ReadJSON(msg)

	if err != nil {
		return "", err
	}

	if msg.Action == ActionIdentify {
		err = json.Unmarshal(msg.Data, &id)

		if err == nil {
			return id, nil
		}
	}

	if id == "" {
		err = errors.New("id cannot be empty")
	}

	return "", err
}

func serverWs(w http.ResponseWriter, r *http.Request) {
	if r.Method != "GET" {
		http.Error(w, "Method not allowed", 405)

		return
	}

	ws, err := upgrader.Upgrade(w, r, nil)

	defer func() {
		ws.Close()
		ws.WriteMessage(websocket.CloseMessage, []byte{})
	}()

	if err != nil {
		log.Println("Dropping client:", err)

		return
	}

	var c *connection

	for {
		id, err := getId(ws)

		if err != nil {
			log.Println("Dropping client:", err)

			return
		}

		r := &request{
			id:   id,
			conn: make(chan *connection),
			err:  make(chan error),
		}

		h.register <- r

		select {
		case c = <-r.conn:
			c.ws = ws
		case err := <-r.err:
			log.Println(err)

			continue
		}

		break
	}

	go c.listenWrite()
	c.listenRead()

	c.active = false
	c.timeout = time.NewTimer(pongWait)

	_, ok := <-c.timeout.C

	if ok {
		h.unregister <- c.id
	}
}
