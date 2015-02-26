package hub

import (
	"encoding/json"
	"errors"
	"log"
	"net/http"
	"sync"
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

type registration struct {
	conn *connection
	ok   chan bool
}

type connection struct {
	aLock  sync.RWMutex
	active bool

	id      string
	timeout *time.Timer
	ws      *websocket.Conn
	send    chan MessageOut
	stop    chan bool
}

type MessageIn struct {
	To     string          `json:"to,omitempty"`
	From   string          `json:"from,omitempty"`
	Action string          `json:"action,omitempty"`
	Data   json.RawMessage `json:"data,omitempty"`
}

type MessageOut struct {
	To     string      `json:"to,omitempty"`
	From   string      `json:"from,omitempty"`
	Action string      `json:"action,omitempty"`
	Data   interface{} `json:"data,omitempty"`
}

func (c *connection) writeMessage(mt int, payload []byte) error {
	c.ws.SetWriteDeadline(time.Now().Add(writeWait))

	return c.ws.WriteMessage(mt, payload)
}

func (c *connection) writeJSON(payload interface{}) error {
	c.ws.SetWriteDeadline(time.Now().Add(writeWait))

	return c.ws.WriteJSON(payload)
}

func (c *connection) listenRead() {
	defer c.ws.Close()

	c.ws.SetReadLimit(maxMessageSize)
	c.ws.SetReadDeadline(time.Now().Add(pongWait))
	c.ws.SetPongHandler(func(string) error {
		c.ws.SetReadDeadline(time.Now().Add(pongWait))

		return nil
	})

	for {
		msg := &MessageIn{}
		err := c.ws.ReadJSON(msg)

		if err != nil {
			if c.id != "" {
				err = errors.New(err.Error() + " (" + c.id + ")")
			}

			log.Println("Dropping client:", err)

			return
		}

		log.Println("Recieved message:", msg)

		h.hLock.RLock()

		for action, cb := range h.handlers {
			if action == msg.Action {
				msg.From = c.id

				cb(*msg)
			}
		}

		h.hLock.RUnlock()

		switch msg.Action {
		case ActionPassthrough:
			data := MessageOut{}
			err := json.Unmarshal(msg.Data, &data)

			if err != nil {
				log.Println("Dropping client:", err)

				return
			}

			h.send <- MessageOut{
				To:     msg.To,
				From:   c.id,
				Action: data.Action,
				Data:   data.Data,
			}

		case ActionIdentify:
			if c.id != "" {
				break
			}

			err := json.Unmarshal(msg.Data, &c.id)

			if err != nil {
				log.Println("Dropping client:", err)

				return
			}

			r := registration{
				conn: c,
				ok:   make(chan bool),
			}

			h.register <- r

			if !<-r.ok {
				c.id = ""
				c.send <- MessageOut{Action: ActionIdentify, Data: "error"}
			} else {
				c.send <- MessageOut{Action: ActionIdentify, Data: "ok"}
			}

		case ActionDisconnect:
			go func() { c.stop <- true }()

			return

		case ActionGetClients:
			h.send <- MessageOut{
				To:     c.id,
				Action: msg.Action,
			}
		}
	}
}

func (c *connection) listenWrite() {
	ticker := time.NewTicker(pingPeriod)

	defer func() {
		c.ws.Close()
		ticker.Stop()
	}()

	for {
		select {
		case m, ok := <-c.send:
			if !ok {
				c.ws.WriteMessage(websocket.CloseMessage, []byte{})

				return
			}

			if err := c.writeJSON(m); err != nil {
				// Putting message back in queue
				m.To = c.id
				go func() { h.send <- m }()

				log.Println("Message put back in queue:", m)
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

func (c *connection) setActive(a bool) {
	c.aLock.Lock()
	defer c.aLock.Unlock()

	c.active = a
}

func (c *connection) isActive() bool {
	c.aLock.RLock()
	defer c.aLock.RUnlock()

	return c.active
}

// ServeWs is run each time a new client connects, it's basically the equvialent of on open.
func ServeWs(w http.ResponseWriter, r *http.Request) {
	if r.Method != "GET" {
		http.Error(w, "Method not allowed", 405)

		return
	}

	ws, err := upgrader.Upgrade(w, r, nil)

	c := &connection{
		active: true,
		ws:     ws,
		send:   make(chan MessageOut),
		stop:   make(chan bool),
	}

	defer func() {
		c.ws.Close()
	}()

	if err != nil {
		log.Println("Dropping client:", err)

		return
	}

	go c.listenWrite()

	c.send <- MessageOut{Action: ActionIdentify}

	c.listenRead()

	c.setActive(false)
	c.timeout = time.NewTimer(h.getTimeout())

	select {
	case <-c.timeout.C:
	case <-c.stop:
	}

	reg := registration{
		conn: c,
		ok:   make(chan bool),
	}

	h.unregister <- reg
	<-reg.ok

	// Empty channel before closing
	for {
		select {
		case <-c.send:
			continue
		default:
		}

		break
	}

	close(c.send)
}
