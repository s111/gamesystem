package gameparser

import "testing"
import "github.com/stretchr/testify/assert"

const (
	gameName    = "Pong"
	description = "game description"
	timeout     = 20
)

var gameExec = exec{"java", "-Djava.library.path=games/pong/lib", "-jar", "games/pong/bin/pong.jar"}

func TestParse(t *testing.T) {
	gp := GameParser{
		Games: make(map[string]Game),
	}

	gp.Parse()

	assert.Len(t, gp.Games, 1)
	assert.NotNil(t, gp.Games[gameName])

	game := gp.Games[gameName]

	assert.Equal(t, gameName, game.Name)
	assert.Equal(t, description, game.Description)
	assert.Equal(t, timeout, game.Timeout)

	cmd, err := game.GetCmd()

	if err != nil {
		assert.Fail(t, "should be platform independet")
	}

	assert.Equal(t, gameExec, cmd)
}
