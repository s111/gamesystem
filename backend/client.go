package main

import "golang.org/x/net/websocket"

var maxId int = 0

type Client struct {
	id     int
	ws     *websocket.Conn
	server *Server
}

func NewClient(ws *websocket.Conn, server *Server) *Client {
	maxId++

	return &Client{maxId, ws, server}
}

func (c *Client) Listen() {
	go c.listenWrite()
	c.listenRead()
}

func (c *Client) listenWrite() {
	for {
		select {}
	}
}

func (c *Client) listenRead() {
	for {
		select {
		default:
			var msg int

			err := websocket.JSON.Receive(c.ws, &msg)

			if err != nil {
				return
			} else {
				if msg > 0 && msg < len(games) && currentGame.Name == "" {
					go func() {
						currentGame = games[msg]
						currentGame.start()
						currentGame = Game{}
					}()
				}
			}
		}
	}
}
