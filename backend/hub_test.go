package main

import (
	"testing"
	"time"

	"github.com/gorilla/websocket"
	"github.com/stretchr/testify/assert"
)

func newTestConnection(id string) *connection {
	return &connection{
		id:      id,
		active:  true,
		timeout: time.NewTimer(pongWait),
		send:    make(chan messageOut),
		ws:      &websocket.Conn{},
	}

}

func newTestRegistration(c *connection) registration {
	return registration{
		conn: c,
		ok:   make(chan bool),
	}
}

func init() {
	go h.run()
}

func TestAdd(t *testing.T) {
	c := newTestConnection("c1")
	r := newTestRegistration(c)

	h.register <- r

	assert.True(t, <-r.ok)
	assert.Len(t, h.clients, 1)
	assert.Equal(t, h.clients[r.conn.id], r.conn)

	c.active = false
	h.unregister <- r

	assert.True(t, <-r.ok)
	assert.Len(t, h.clients, 0)
}

func TestAddWithExistingId(t *testing.T) {
	c1 := newTestConnection("c1")
	r1 := newTestRegistration(c1)
	c2 := newTestConnection("c1")
	r2 := newTestRegistration(c2)

	h.register <- r1

	assert.True(t, <-r1.ok)
	assert.Len(t, h.clients, 1)
	assert.Equal(t, h.clients[r1.conn.id], r1.conn)

	h.register <- r2

	assert.False(t, <-r2.ok, "The new connection should not overwrite the old one")
	assert.Len(t, h.clients, 1)
	assert.Equal(t, h.clients[r1.conn.id], r1.conn, "The connection should not be overwritten")

	c1.active = false
	h.unregister <- r1

	assert.True(t, <-r1.ok)
	assert.Len(t, h.clients, 0)
}

func TestAddWithExistingIdWhenInactive(t *testing.T) {
	c1 := newTestConnection("c1")
	r1 := newTestRegistration(c1)
	c2 := newTestConnection("c1")
	r2 := newTestRegistration(c2)

	h.register <- r1

	assert.True(t, <-r1.ok)
	assert.Len(t, h.clients, 1)
	assert.Equal(t, h.clients[r1.conn.id], r1.conn)

	c1.setActive(false)

	m1 := messageOut{From: game, To: "c1", Data: "message1"}
	m2 := messageOut{From: game, To: "c1", Data: "message2"}

	h.send <- m1
	h.send <- m2

	h.register <- r2

	m1.To = ""
	m2.To = ""

	assert.Equal(t, <-c2.send, m1, "first message not received correctly")
	assert.Equal(t, <-c2.send, m2, "second message not received correctly")
	assert.True(t, <-r2.ok, "The new connection should overwrite the old one")
	assert.Len(t, h.clients, 1)
	assert.Equal(t, h.clients[r1.conn.id], r2.conn, "The new connection should overwrite the old one")

	c2.setActive(false)
	h.unregister <- r2

	assert.True(t, <-r2.ok)
	assert.Len(t, h.clients, 0)
}
