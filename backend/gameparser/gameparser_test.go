package gameparser

import "testing"
import "github.com/stretchr/testify/assert"

const (
	gameName = "Pong"
)

var gameExec = []string{"java", "-Djava.library.path=games/pong/lib", "-jar", "games/pong/bin/pong.jar"}

func TestParse(t *testing.T) {
	gp := GameParser{
		Games: make(map[string]Game),
	}

	gp.Parse()

	assert.Len(t, gp.Games, 1)
	assert.NotNil(t, gp.Games[gameName])
	assert.Equal(t, gp.Games[gameName].Name, gameName)
	assert.Equal(t, gp.Games[gameName].Exec, gameExec)
}
