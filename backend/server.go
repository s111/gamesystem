package main

import (
	"fmt"
	"net/http"

	"golang.org/x/net/websocket"
)

type Server struct {
	pattern string
	clients map[int]*Client
	addCh   chan *Client
	errCh   chan error
}

func NewServer(pattern string) *Server {
	clients := make(map[int]*Client)
	addCh := make(chan *Client)
	errCh := make(chan error)

	return &Server{
		pattern,
		clients,
		addCh,
		errCh,
	}
}

func (s *Server) Add(c *Client) {
	s.addCh <- c
}

func (s *Server) Listen() {
	onConnected := func(ws *websocket.Conn) {
		defer func() {
			err := ws.Close()

			if err != nil {
				s.errCh <- err
			}
		}()

		websocket.JSON.Send(ws, games)

		client := NewClient(ws, s)
		s.Add(client)
		client.Listen()
	}

	handler := func(w http.ResponseWriter, r *http.Request) {
		s := websocket.Server{Handler: websocket.Handler(onConnected)}
		s.ServeHTTP(w, r)
	}

	http.HandleFunc(s.pattern, handler)

	for {
		select {
		case c := <-s.addCh:
			fmt.Println("Added new client")
			s.clients[c.id] = c

		case err := <-s.errCh:
			fmt.Println("Error: ", err.Error())
		}
	}
}
