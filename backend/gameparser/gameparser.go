package gameparser

import (
	"encoding/json"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
)

const (
	GamesDir      = "games"
	ControllerDir = "controller"

	gameDescription = "game.json"
)

type Game struct {
	Name string
	Exec []string
}

type GameParser struct {
	Games map[string]Game
}

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
