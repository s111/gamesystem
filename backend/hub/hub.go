// Package hub manages communication between clients (game, controllers and the program running the hub).
package hub

import (
	"log"
	"sync"
	"time"
)

const (
	// ActionIdentify should be sent to ask a client to identify itself.
	// The client should respond with the same action and data: uid.
	ActionIdentify = "identify"

	// ActionPassthrough should be used when the message is not directly for the hub.
	// Message should be on the format:
	// {to: receiver, action: pass through, data: {action: actual action, data: actual data}}
	ActionPassthrough = "pass through"

	// ActionDisconnect is a action used when a client wishes to immeaditaly terminate its connection.
	ActionDisconnect = "disconnect"

	// ActionGetClients is a action used when asking for the hubs current list of clients.
	ActionGetClients = "get clients"

	// ActionSetUsername is a action used to set a username for the client
	ActionSetUsername = "set username"

	// ActionGetUsername is a action used to get the username of a client id
	ActionGetUsername = "get username"

	// EventAdd is the event of adding a client.
	EventAdd = "added client"

	// EventResume is the event of resuming a connection before it is dropped.
	EventResume = "resumed client"

	// EventDrop is the event of dropping a client.
	EventDrop = "dropped client"

	// EventUsernameChange is the event of a client changing username.
	EventUsernameChange = "changed username"

	// Broadcast is used in the To field of a message to broadcast it to all users except the sender.
	Broadcast = "all"
)

var h = hub{
	clients:       make(map[string]*connection),
	usernames:     make(map[string]string),
	register:      make(chan registration),
	unregister:    make(chan registration),
	send:          make(chan MessageOut),
	msgHandlers:   make(map[string]func(MessageIn)),
	eventHandlers: make(map[string]func(string)),
}

type hub struct {
	clients   map[string]*connection
	usernames map[string]string

	register   chan registration
	unregister chan registration

	send chan MessageOut

	tLock   sync.RWMutex
	timeout time.Duration

	mLock       sync.RWMutex
	msgHandlers map[string]func(MessageIn)

	eLock         sync.RWMutex
	eventHandlers map[string]func(string)
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
					// Transfer username
					r.conn.username = c.username

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

					go runEventHandler(EventResume, r.conn.id)

					log.Println("Resumed client:", r.conn.id)
				} else {
					r.ok <- false
				}
			} else {
				if r.conn.id != "" {
					h.clients[r.conn.id] = r.conn

					r.ok <- true

					go runEventHandler(EventAdd, r.conn.id)

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
					delete(h.usernames, c.username)

					go runEventHandler(EventDrop, r.conn.id)

					log.Println("Dropped client:", r.conn.id)
				}
			}

			r.ok <- true

		case m := <-h.send:
			switch m.Action {
			case ActionGetClients:
				var clients []string

				for id := range h.clients {
					if id == m.To {
						continue
					}

					clients = append(clients, id)
				}

				go func() {
					if c, ok := h.clients[m.To]; ok {
						c.send <- MessageOut{
							Action: ActionGetClients,
							Data:   clients,
						}
					}
				}()

			case ActionSetUsername:
				c := h.clients[m.From]
				username := m.Data.(string)

				if _, ok := h.usernames[username]; ok {
					c.send <- MessageOut{
						Action: m.Action,
						Data:   "error",
					}
				} else {
					h.usernames[username] = m.From
					c.username = username

					c.send <- MessageOut{
						Action: m.Action,
						Data:   "ok",
					}

					go runEventHandler(EventUsernameChange, c.id)
				}

			case ActionGetUsername:
				id := m.Data.(string)
				to := m.From

				if m.To != "" {
					to = m.To
				}

				if c, ok := h.clients[id]; ok {
					if c.username != "" {
						h.clients[to].send <- MessageOut{
							Action: m.Action,
							Data:   []string{id, c.username},
						}

						break
					}
				}

				h.clients[to].send <- MessageOut{
					Action: m.Action,
					Data:   []string{id, "user-" + id},
				}

			default:
				if m.To == Broadcast {
					m.To = ""

					for _, c := range h.clients {
						if c.id == m.From {
							continue
						}

						// Use a go routine as a send can block when the connection is inactive
						go func(c *connection) { c.send <- m }(c)
					}
				} else if c, ok := h.clients[m.To]; ok {
					m.To = ""

					// Use a go routine as a send can block when the connection is inactive
					go func() { c.send <- m }()
				}
			}
		}
	}
}

func runEventHandler(event string, id string) {
	h.eLock.RLock()
	defer h.eLock.RUnlock()

	if cb, ok := h.eventHandlers[event]; ok {
		cb(id)
	}
}

// AddMessageHandler allows your progam to register callbacks for specific actions.
func AddMessageHandler(action string, cb func(m MessageIn)) {
	h.mLock.Lock()
	defer h.mLock.Unlock()

	h.msgHandlers[action] = cb
}

// AddEventHandler allows your progam to register callbacks for specific events.
func AddEventHandler(event string, cb func(id string)) {
	h.eLock.Lock()
	defer h.eLock.Unlock()

	h.eventHandlers[event] = cb

}

// Send allows your program to send a message through the hub.
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
