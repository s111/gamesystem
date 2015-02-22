// Package hub manages communication between clients (game, controllers and the program running the hub).
package hub

import (
	"log"
	"sync"
	"time"
)

const (
	// Game must identify with this string
	Game = "game"

	// ActionIdentify should be sent to ask a client to identify itself.
	// The client should respond with the same action and data: uid.
	ActionIdentify = "identify"

	// ActionPassthrough should be used when the message is not directly for the hub.
	// Message should be on the format:
	// {to: receiver, action: passthrough, data: {action: actual action, data: actual data}}
	ActionPassthrough = "passthrough"

	// ActionAdd is a event sent to the game when a client is added
	ActionAdd = "added client"

	// ActionDrop is a event sent to the game when a client is dropped
	ActionDrop = "dropped client"

	ActionList  = "list"
	ActionStart = "start"
)

var h = hub{
	clients:    make(map[string]*connection),
	register:   make(chan registration),
	unregister: make(chan registration),
	send:       make(chan MessageOut),
	handlers:   make(map[string]func(MessageIn)),
}

type hub struct {
	clients    map[string]*connection
	register   chan registration
	unregister chan registration
	send       chan MessageOut

	tLock   sync.RWMutex
	timeout time.Duration

	hLock    sync.RWMutex
	handlers map[string]func(MessageIn)
}

func (h *hub) setTimeout(d time.Duration) {
	h.tLock.Lock()
	defer h.tLock.Unlock()

	h.timeout = d
}

func (h *hub) getTimeout() time.Duration {
	h.tLock.RLock()
	defer h.tLock.RUnlock()

	return h.timeout
}

func (h *hub) run() {
	for {
		select {
		case r := <-h.register:
			if c, ok := h.clients[r.conn.id]; ok {
				if !c.isActive() {
					// Speed up the closing of the old connection
					select {
					case c.stop <- true:
					default:
					}

					h.clients[r.conn.id] = r.conn

					// Passing messages to the new channel
					for {
						select {
						case m := <-c.send:
							r.conn.send <- m

							continue
						default:
						}

						break
					}

					r.ok <- true

					log.Println("Resumed client:", r.conn.id)
				} else {
					r.ok <- false
				}
			} else {
				if r.conn.id != "" {
					h.clients[r.conn.id] = r.conn

					r.ok <- true

					go func() {
						if r.conn.id != Game {
							h.send <- MessageOut{
								To:     Game,
								Action: ActionAdd,
								Data:   r.conn.id,
							}
						}
					}()

					log.Println("Added client:", r.conn.id)
				} else {
					r.ok <- false

					log.Println("Id cannot be empty")
				}
			}

		case r := <-h.unregister:
			if c, ok := h.clients[r.conn.id]; ok {
				if !c.isActive() {
					delete(h.clients, r.conn.id)

					go func() {
						if c.id != Game {
							h.send <- MessageOut{
								To:     Game,
								Action: ActionDrop,
								Data:   c.id,
							}
						}
					}()

					log.Println("Dropped client:", r.conn.id)
				}
			}

			r.ok <- true

		case m := <-h.send:
			if c, ok := h.clients[m.To]; ok {
				m.To = ""

				// Use a go routine as a send can block when the connection is inactive
				go func() { c.send <- m }()
			}
		}
	}
}

func AddMessageHandler(action string, cb func(m MessageIn)) {
	h.hLock.Lock()
	defer h.hLock.Unlock()

	h.handlers[action] = cb
}

func Send(m MessageOut) {
	h.send <- m
}

// SetTimeout is used to set how long a client can be "gone" until the game should consider it dropped.
func SetTimeout(d time.Duration) {
	h.setTimeout(d)
}

// Run starts the hub.
// Should be in its own go routine unless you want it to block.
func Run() {
	h.run()
}
