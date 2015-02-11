package main

import "log"

const game = "game"

type hub struct {
	connections map[*connection]string
	clients     map[string]*connection
	register    chan *connection
	unregister  chan *connection
	sendToGame  chan messageOut
}

var h = hub{
	connections: make(map[*connection]string),
	clients:     make(map[string]*connection),
	register:    make(chan *connection),
	unregister:  make(chan *connection),
	sendToGame:  make(chan messageOut),
}

func (h *hub) run() {
	for {
		select {
		case c := <-h.register:
			if h.clients[c.id] != c {
				if oldC, ok := h.clients[c.id]; ok {
					close(oldC.send)
					delete(h.connections, oldC)

					log.Printf("New connection claiming to be %v, old connection closed", c.id)
				}
			}

			if id, ok := h.connections[c]; ok {
				if id == c.id {
					break
				}

				if _, ok = h.clients[id]; ok {
					delete(h.clients, id)

					h.clients[c.id] = c
					h.connections[c] = c.id

					log.Println("Renamed", id, "to", c.id)

					// TODO: Notify game
				}

				break
			}

			h.clients[c.id] = c
			h.connections[c] = c.id

			log.Println("Added client:", c.id)

			// TODO: Notify game

		case c := <-h.unregister:
			if _, ok := h.connections[c]; ok {
				close(c.send)
				delete(h.connections, c)
				delete(h.clients, c.id)
			}

		case m := <-h.sendToGame:
			if c, ok := h.clients[game]; ok {
				c.send <- m
			}
		}
	}
}
