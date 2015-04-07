// Package gameparser allows you to discover games in your game directory.
package gameparser

import (
	"encoding/json"
	"errors"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
	"runtime"
)

const (
	// GamesDir is the name of the directory relative to the working directory of your program, which contains the games.
	GamesDir = "games"

	// ControllerDir is the name of the directory in the games path where the controller resides.
	ControllerDir = "controller"

	gameDescription = "game.json"
)

// ErrNotSupported is used when a game is not supported on the current system
var ErrNotSupported = errors.New("gameparser: Game is not supported on this system")

type exec []string
type arch map[string]exec

// GameParser holds the parsed games.
// It'll be empty until Parse is called.
type GameParser struct {
	Games map[string]Game
}

// Game should contain a games name and how to execute it.
// LaunchOptions describes how to execute the game on different systems
// Description is a short description of the game
// Timeout is the amount of time before a client is considered dropped
type Game struct {
	Name          string
	Description   string
	LaunchOptions launchOptions `json:"launch-options"`
	Timeout       int
}

type launchOptions struct {
	All        []string
	OsSpecific map[string]arch `json:"os-specific"`
}

// GetCmd first tries to return the most specific command, then the platform independent one, if none are found a error is returned
func (g *Game) GetCmd() (exec, error) {
	if len(g.LaunchOptions.OsSpecific) > 0 {
		if o, ok := g.LaunchOptions.OsSpecific[runtime.GOOS]; ok {
			if a, ok := o[runtime.GOARCH]; ok {
				return a, nil
			}
		}
	}

	if len(g.LaunchOptions.All) > 0 {
		return g.LaunchOptions.All, nil
	}

	return nil, ErrNotSupported
}

// Parse will look through the GamesDir and parse the game description.
// Found games are added to GameParser.Games.
func (gp *GameParser) Parse() {
	names, _ := ioutil.ReadDir(GamesDir)

	for _, name := range names {
		gamePath := filepath.Join(GamesDir, name.Name())
		filename := filepath.Join(gamePath, gameDescription)

		if _, err := os.Stat(filename); os.IsNotExist(err) {
			log.Printf("Found directory without %v: %v\n", gameDescription, gamePath)

			continue
		}

		file, err := ioutil.ReadFile(filename)

		if err != nil {
			log.Printf("Could not read %v\n", gameDescription)

			continue
		}

		var game Game
		json.Unmarshal(file, &game)

		gp.Games[game.Name] = game
	}
}
