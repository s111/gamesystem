package main

import "log"

const (
	game = "game"

	ActionIdentify    = "identify"
	ActionPassthrough = "passthrough"
	ActionAdd         = "added client"
	ActionDrop        = "dropped client"
	ActionRename      = "renamed client"
)

type hub struct {
	connections map[*connection]string
	clients     map[string]*connection
	register    chan *connection
	unregister  chan *connection
	send        chan messageOut
}

var h = hub{
	connections: make(map[*connection]string),
	clients:     make(map[string]*connection),
	register:    make(chan *connection),
	unregister:  make(chan *connection),
	send:        make(chan messageOut),
}

func (h *hub) run() {
	sendMessage := func(m messageOut) {
		h.send <- m
	}

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

					go sendMessage(messageOut{
						To:     game,
						Action: ActionRename,
						Data:   []string{id, c.id},
					})
				}

				break
			}

			h.clients[c.id] = c
			h.connections[c] = c.id

			log.Println("Added client:", c.id)

			go sendMessage(messageOut{
				To:     game,
				Action: ActionAdd,
				Data:   c.id,
			})

		case c := <-h.unregister:
			if _, ok := h.connections[c]; ok {
				close(c.send)
				delete(h.connections, c)
				delete(h.clients, c.id)
			}

		case m := <-h.send:
			if c, ok := h.clients[m.To]; ok {
				c.send <- m
			}
		}
	}
}
