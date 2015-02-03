package main

type hub struct {
	connections map[*connection]bool
	register    chan *connection
	unregister  chan *connection
	broadcast   chan string
}

var h = hub{
	connections: make(map[*connection]bool),
	register:    make(chan *connection),
	unregister:  make(chan *connection),
	broadcast:   make(chan string),
}

func (h *hub) run() {
	closeConn := func(c *connection) {
		delete(h.connections, c)

		close(c.sendList)
		close(c.sendReady)
	}

	for {
		select {
		case c := <-h.register:
			h.connections[c] = true
		case c := <-h.unregister:
			if _, ok := h.connections[c]; ok {
				closeConn(c)
			}
		case s := <-h.broadcast:
			for c := range h.connections {
				select {
				case c.sendReady <- s:
				default:
					closeConn(c)
				}
			}
		}
	}
}
