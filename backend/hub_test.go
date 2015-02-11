package main

import (
	"testing"

	"github.com/gorilla/websocket"
)

const (
	clientId = "client"
	oldId    = "client1"
	newId    = "client2"
)

func init() {
	go h.run()
}

func register(c *connection) {
	h.register <- c
	// Blocks until the first connection is registered, this one should have no effect
	h.register <- c
}

func unregister(c *connection) {
	h.unregister <- c
	// Blocks until the first connection is unregistered, this one should have no effect
	h.unregister <- c
}

func newConnection(id string) *connection {
	return &connection{
		id:   id,
		send: make(chan message),
		ws:   &websocket.Conn{},
	}
}

func checkCCReferences(t *testing.T, id string, c *connection) {
	if _, ok := h.clients[id]; ok {
		if h.clients[id] != c {
			t.Fatalf("Client name does not refer to the correct connection")
		}
	} else {
		t.Fatalf("Client name not added to map")
	}

	if _, ok := h.connections[c]; ok {
		if h.connections[c] != id {
			t.Fatalf("Connection does not refer to the correct client name")
		}
	} else {
		t.Fatalf("Connection not added to map")
	}

}

func checkCCLen(t *testing.T, n int) {
	if l := len(h.connections); l != n {
		t.Fatalf("len(h.connections)=%v, want %v", l, n)
	}

	if l := len(h.clients); l != n {
		t.Fatalf("len(h.clients)=%v, want %v", l, n)
	}
}

func TestRegister(t *testing.T) {
	conn := newConnection(clientId)

	register(conn)

	checkCCReferences(t, clientId, conn)

	checkCCLen(t, 1)
	unregister(conn)
	checkCCLen(t, 0)
}

func TestRename(t *testing.T) {
	conn := newConnection(oldId)

	register(conn)
	conn.id = newId
	register(conn)

	checkCCReferences(t, newId, conn)

	checkCCLen(t, 1)
	unregister(conn)
	checkCCLen(t, 0)
}

func TestOverwrite(t *testing.T) {
	conn1 := newConnection(clientId)
	conn2 := newConnection(clientId)

	register(conn1)
	register(conn2)

	checkCCReferences(t, clientId, conn2)

	checkCCLen(t, 1)
	unregister(conn2)
	checkCCLen(t, 0)
}
