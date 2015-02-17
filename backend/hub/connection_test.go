package hub

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gorilla/websocket"
	"github.com/stretchr/testify/assert"
)

type wsHandler struct{ *testing.T }

func (t wsHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	ServeWs(w, r)
}

type Server struct {
	*httptest.Server
	URL string
}

func newServer(t *testing.T) *Server {
	var s Server
	s.Server = httptest.NewServer(wsHandler{t})
	s.URL = "ws" + s.Server.URL[len("http"):]

	return &s
}

func TestDial(t *testing.T) {
	s := newServer(t)
	defer s.Close()

	ws, _, err := websocket.DefaultDialer.Dial(s.URL, nil)

	if err != nil {
		t.Fatalf("Dial: %v", err)
	}

	ws.WriteJSON(messageOut{
		Action: ActionIdentify,
		Data:   "game",
	})

	ws.WriteJSON(messageOut{
		Action: ActionPassthrough,
		To:     Game,
		Data:   "Hello, Game!",
	})

	defer ws.Close()
}

func TestOrigin(t *testing.T) {
	assert.Equal(t, upgrader.CheckOrigin(&http.Request{}), true, "CheckOrigin should always return true")
}
