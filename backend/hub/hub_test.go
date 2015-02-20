package hub

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

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

func newWs(t *testing.T, url string) *websocket.Conn {
	ws, _, err := websocket.DefaultDialer.Dial(url, nil)

	assert.Nil(t, err, "Dial: %v", err)

	return ws
}

func sendCloseMessage(t *testing.T, ws *websocket.Conn) {
	err := ws.WriteMessage(websocket.CloseMessage, []byte{})

	assert.Nil(t, err, "ws: %v", err)

	wait(10)
}

func wait(ms int) {
	<-time.After(time.Millisecond * time.Duration(ms))
}

func checkClientRegistered(t *testing.T, id string) {
	wait(20)

	assert.NotNil(t, h.clients[id])
}

func checkClientUnregistered(t *testing.T, id string) {
	wait(40)

	assert.Nil(t, h.clients[id])
}

func TestAddClient(t *testing.T) {
	s := newServer(t)
	defer s.Close()

	ws := newWs(t, s.URL)
	defer ws.Close()

	ws.WriteJSON(messageOut{
		Action: ActionIdentify,
		Data:   Game,
	})

	checkClientRegistered(t, Game)
	sendCloseMessage(t, ws)

	h.send <- messageOut{
		To: Game,
	}

	checkClientUnregistered(t, Game)
}

func TestAddClientEmptyId(t *testing.T) {
	s := newServer(t)
	defer s.Close()

	ws := newWs(t, s.URL)
	defer ws.Close()

	ws.WriteJSON(messageOut{
		Action: ActionIdentify,
		Data:   "",
	})

	wait(20)
	assert.Empty(t, h.clients)
}

func TestReplaceActiveClient(t *testing.T) {
	s := newServer(t)
	defer s.Close()

	ws1 := newWs(t, s.URL)
	ws2 := newWs(t, s.URL)
	defer ws1.Close()
	defer ws2.Close()

	ws1.WriteJSON(messageOut{
		Action: ActionIdentify,
		Data:   Game,
	})

	checkClientRegistered(t, Game)

	ws2.WriteJSON(messageOut{
		Action: ActionIdentify,
		Data:   Game,
	})

	wait(20)

	msg := &messageIn{}

	// first identify message
	ws2.ReadJSON(msg)
	// second identify message
	ws2.ReadJSON(msg)

	assert.Equal(t, ActionIdentify, msg.Action)

	sendCloseMessage(t, ws1)
	sendCloseMessage(t, ws2)
	checkClientUnregistered(t, Game)
}

func TestResumeClientWithQueuedMessage(t *testing.T) {
	s := newServer(t)
	defer s.Close()

	ws := newWs(t, s.URL)

	ws.WriteJSON(messageOut{
		Action: ActionIdentify,
		Data:   Game,
	})

	checkClientRegistered(t, Game)
	sendCloseMessage(t, ws)

	ws.Close()

	h.send <- messageOut{
		To: Game,
	}

	ws = newWs(t, s.URL)

	ws.WriteJSON(messageOut{
		Action: ActionIdentify,
		Data:   Game,
	})

	checkClientRegistered(t, Game)
	sendCloseMessage(t, ws)
	checkClientUnregistered(t, Game)

	ws.Close()
}

func TestPassthrough(t *testing.T) {
	s := newServer(t)
	defer s.Close()

	const (
		c            = "c1"
		actionActual = "actual action"
	)

	gameWs := newWs(t, s.URL)
	cWs := newWs(t, s.URL)
	defer gameWs.Close()
	defer cWs.Close()

	gameWs.WriteJSON(messageOut{
		Action: ActionIdentify,
		Data:   Game,
	})

	checkClientRegistered(t, Game)

	cWs.WriteJSON(messageOut{
		Action: ActionIdentify,
		Data:   c,
	})

	checkClientRegistered(t, c)

	cWs.WriteMessage(
		websocket.TextMessage,
		[]byte(`{"to":"`+Game+`", "action":"`+ActionPassthrough+`", "data":{"action": "`+actionActual+`", "data": ""}}`),
	)

	wait(20)

	msg := &messageIn{}
	gameWs.ReadJSON(msg)
	// The second should be a added client message
	gameWs.ReadJSON(msg)
	// The third should be the message from c
	gameWs.ReadJSON(msg)

	assert.Equal(t, msg.Action, actionActual)

	sendCloseMessage(t, gameWs)
	sendCloseMessage(t, cWs)
	checkClientUnregistered(t, Game)
	checkClientUnregistered(t, c)
}

func TestOrigin(t *testing.T) {
	assert.Equal(t, upgrader.CheckOrigin(&http.Request{}), true, "CheckOrigin should always return true")
}

func init() {
	SetTimeout(time.Millisecond * 20)
	go Run()
}
