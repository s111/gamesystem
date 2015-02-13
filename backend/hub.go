package main

import (
	"encoding/json"
	"errors"
	"log"
)

const (
	game = "game"

	ActionIdentify    = "identify"
	ActionPassthrough = "passthrough"
	ActionAdd         = "added client"
	ActionDrop        = "dropped client"
)

type hub struct {
	clients    map[string]*connection
	register   chan *request
	unregister chan string
}

var h = hub{
	clients:    make(map[string]*connection),
	register:   make(chan *request),
	unregister: make(chan string),
}

func (h *hub) run() {
	for {
		select {
		case r := <-h.register:
			if c, ok := h.clients[r.id]; ok {
				if !c.active {
					c.active = true
					c.timeout.Stop()

					r.conn <- c

					log.Println("Resuming client:", r.id)
				} else {
					r.err <- errors.New("Id in use: " + r.id)
				}
			} else {
				c := &connection{
					id:     r.id,
					active: true,
					send:   make(chan messageOut),
				}

				h.clients[r.id] = c

				r.conn <- c

				h.send(game, messageOut{
					Action: ActionAdd,
					Data:   c.id,
				})

				log.Println("Adding client:", r.id)
			}

		case id := <-h.unregister:
			if c, ok := h.clients[id]; ok {
				if !c.active {
					c.timeout.Stop()

					// Discard all pending sends
					for {
						select {
						case <-c.send:
							continue
						default:
						}

						break
					}

					close(c.send)
					delete(h.clients, c.id)

					log.Println("Removing client:", c.id)

					h.send(game, messageOut{
						Action: ActionDrop,
						Data:   c.id,
					})

				}
			}
		}
	}
}

func (h *hub) send(to string, m messageOut) {
	if c, ok := h.clients[to]; ok {
		// Use a go routine as a send can block when the connection is inactive
		go func() { c.send <- m }()
	} else {
		log.Println("Tried to send message to nonexistent client:", m.To)
	}
}

func (h *hub) passthrough(to string, from string, data *json.RawMessage) {
	// Don't allow clients to talk to each other
	if to == game || from == game {
		h.send(to, messageOut{
			From:   from,
			Action: ActionPassthrough,
			Raw:    data,
		})
	}
}
