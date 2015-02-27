package hub

import (
	"encoding/json"
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

func checkNoneRegistered(t *testing.T, url string) {
	clients := getClients(t, url)

	assert.Len(t, clients, 1)
}

func checkClientRegistered(t *testing.T, url string, id string) {
	clients := getClients(t, url)

	assert.Contains(t, clients, id)
}

func checkClientUnregistered(t *testing.T, url string, id string) {
	clients := getClients(t, url)

	assert.NotContains(t, clients, id)
}

func getClients(t *testing.T, url string) []string {
	wait(40)

	ws := newWs(t, url)
	defer ws.Close()

	ws.WriteJSON(MessageOut{
		Action: ActionIdentify,
		Data:   "clientcheck",
	})

	wait(20)

	ws.WriteJSON(MessageOut{
		Action: ActionGetClients,
	})

	wait(20)

	msg := &MessageIn{}
	// identify message
	ws.ReadJSON(msg)
	// identify ok message
	ws.ReadJSON(msg)
	// get clients message
	ws.ReadJSON(msg)

	assert.Equal(t, ActionGetClients, msg.Action, "You have probably forgotten to read the buffered messages")

	var clients []string
	json.Unmarshal(msg.Data, &clients)

	ws.Close()
	wait(40)

	return clients
}

func TestAddClient(t *testing.T) {
	s := newServer(t)
	defer s.Close()

	ws := newWs(t, s.URL)
	defer ws.Close()

	ws.WriteJSON(MessageOut{
		Action: ActionIdentify,
		Data:   Game,
	})

	checkClientRegistered(t, s.URL, Game)
	sendCloseMessage(t, ws)

	h.send <- MessageOut{
		To: Game,
	}

	checkClientUnregistered(t, s.URL, Game)
}

func TestAddClientEmptyId(t *testing.T) {
	s := newServer(t)
	defer s.Close()

	ws := newWs(t, s.URL)
	defer ws.Close()

	ws.WriteJSON(MessageOut{
		Action: ActionIdentify,
		Data:   "",
	})

	wait(20)

	checkNoneRegistered(t, s.URL)
}

func TestReplaceActiveClient(t *testing.T) {
	s := newServer(t)
	defer s.Close()

	ws1 := newWs(t, s.URL)
	ws2 := newWs(t, s.URL)
	defer ws1.Close()
	defer ws2.Close()

	ws1.WriteJSON(MessageOut{
		Action: ActionIdentify,
		Data:   Game,
	})

	checkClientRegistered(t, s.URL, Game)

	ws2.WriteJSON(MessageOut{
		Action: ActionIdentify,
		Data:   Game,
	})

	wait(20)

	msg := &MessageIn{}
	// first identify message
	ws2.ReadJSON(msg)
	// second identify message
	ws2.ReadJSON(msg)

	assert.Equal(t, ActionIdentify, msg.Action)

	sendCloseMessage(t, ws1)
	sendCloseMessage(t, ws2)
	checkClientUnregistered(t, s.URL, Game)
}

func TestResumeClientWithQueuedMessage(t *testing.T) {
	s := newServer(t)
	defer s.Close()

	ws := newWs(t, s.URL)

	ws.WriteJSON(MessageOut{
		Action: ActionIdentify,
		Data:   Game,
	})

	checkClientRegistered(t, s.URL, Game)
	sendCloseMessage(t, ws)

	ws.Close()

	h.send <- MessageOut{
		To: Game,
	}

	ws = newWs(t, s.URL)

	ws.WriteJSON(MessageOut{
		Action: ActionIdentify,
		Data:   Game,
	})

	checkClientRegistered(t, s.URL, Game)
	sendCloseMessage(t, ws)
	checkClientUnregistered(t, s.URL, Game)

	ws.Close()
}

func TestPassthrough(t *testing.T) {
	s := newServer(t)
	defer s.Close()

	const (
		c            = "c1"
		game         = "game1"
		actionActual = "actual action"
	)

	gameWs := newWs(t, s.URL)
	cWs := newWs(t, s.URL)
	defer gameWs.Close()
	defer cWs.Close()

	gameWs.WriteJSON(MessageOut{
		Action: ActionIdentify,
		Data:   game,
	})

	checkClientRegistered(t, s.URL, game)

	cWs.WriteJSON(MessageOut{
		Action: ActionIdentify,
		Data:   c,
	})

	checkClientRegistered(t, s.URL, c)

	cWs.WriteMessage(
		websocket.TextMessage,
		[]byte(`{"to":"`+game+`", "action":"`+ActionPassthrough+`", "data":{"action": "`+actionActual+`", "data": ""}}`),
	)

	wait(20)

	msg := &MessageIn{}
	// identify message
	gameWs.ReadJSON(msg)
	// identify ok message
	gameWs.ReadJSON(msg)
	// passthrough message
	gameWs.ReadJSON(msg)

	assert.Equal(t, msg.Action, actionActual)

	sendCloseMessage(t, gameWs)
	sendCloseMessage(t, cWs)
	checkClientUnregistered(t, s.URL, game)
	checkClientUnregistered(t, s.URL, c)
}

func TestOrigin(t *testing.T) {
	assert.Equal(t, upgrader.CheckOrigin(&http.Request{}), true, "CheckOrigin should always return true")
}

func init() {
	SetTimeout(time.Millisecond * 20)
	go Run()
}
