package hub

import (
	"log"
	"sync"
	"time"
)

const (
	Game = "game"

	ActionIdentify    = "identify"
	ActionPassthrough = "passthrough"
	ActionAdd         = "added client"
	ActionDrop        = "dropped client"
)

var h = hub{
	clients:    make(map[string]*connection),
	register:   make(chan registration),
	unregister: make(chan registration),
	send:       make(chan messageOut),
}

type hub struct {
	clients    map[string]*connection
	register   chan registration
	unregister chan registration
	send       chan messageOut

	tLock   sync.RWMutex
	timeout time.Duration
}

func (h *hub) setTimeout(d time.Duration) {
	h.tLock.RLock()
	defer h.tLock.RUnlock()

	h.timeout = d
}

func (h *hub) getTimeout() time.Duration {
	h.tLock.Lock()
	defer h.tLock.Unlock()

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
						h.send <- messageOut{
							To:     Game,
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
							To:     Game,
							Action: ActionDrop,
							Data:   c.id,
						}
					}()

					log.Println("Dropped client:", r.conn.id)
				}
			}

			r.ok <- true

		case m := <-h.send:
			if c, ok := h.clients[m.To]; ok {
				// Don't allow clients to talk to each other
				if m.To == Game || m.From == Game {
					m.To = ""

					// Use a go routine as a send can block when the connection is inactive
					go func() { c.send <- m }()
				}
			}
		}
	}
}

func SetTimeout(d time.Duration) {
	h.setTimeout(d)
}

func Run() {
	h.run()
}
