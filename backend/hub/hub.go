package hub

import "log"

const (
	game = "game"

	ActionIdentify    = "identify"
	ActionPassthrough = "passthrough"
	ActionAdd         = "added client"
	ActionDrop        = "dropped client"
)

type hub struct {
	clients    map[string]*connection
	register   chan registration
	unregister chan registration
	send       chan messageOut
}

var h = hub{
	clients:    make(map[string]*connection),
	register:   make(chan registration),
	unregister: make(chan registration),
	send:       make(chan messageOut),
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
						h.send <- messageOut{
							To:     game,
							Action: ActionAdd,
							Data:   r.conn.id,
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
						h.send <- messageOut{
							To:     game,
							Action: ActionDrop,
							Data:   c.id,
						}
					}()

					log.Println("Dropped client:", r.conn.id)
				}

				r.ok <- true
			}

		case m := <-h.send:
			if c, ok := h.clients[m.To]; ok {
				// Don't allow clients to talk to each other
				if m.To == game || m.From == game {
					m.To = ""

					// Use a go routine as a send can block when the connection is inactive
					go func() { c.send <- m }()
				}
			}
		}
	}
}

func Run() {
	h.run()
}
