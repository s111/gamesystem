package gameparser

import (
	"encoding/json"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
)

const (
	// GamesDir is the name of the directory relative to the working directory of your program, which contains the games.
	GamesDir = "games"

	// ControllerDir is the name of the directory in the games path where the controller resides.
	ControllerDir = "controller"

	gameDescription = "game.json"
)

// Game should contain a games name and how to execute it.
// Game.Exec should contain [program, args...].
type Game struct {
	Name string
	Exec []string
}

// GameParser holds the parsed games.
// It'll be empty until Parse is called.
type GameParser struct {
	Games map[string]Game
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
