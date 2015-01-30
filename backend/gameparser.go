package main

import (
	"encoding/json"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
)

const (
	gameDescription = "game.json"
	gamesDir        = "games"
	controllerDir   = "controller"
)

func parseGames() (Game, []Game) {
	var launcher Game
	var games []Game

	names, _ := ioutil.ReadDir(gamesDir)

	for _, name := range names {
		gamePath := filepath.Join(gamesDir, name.Name())
		filename := filepath.Join(gamePath, gameDescription)

		if _, err := os.Stat(filename); os.IsNotExist(err) {
			log.Printf("Warning: Found directory without %v: %v", gameDescription, gamePath)

			continue
		}

		file, err := ioutil.ReadFile(filename)

		if err != nil {
			log.Printf("Warning: Could not read %v", gameDescription)

			continue
		}

		var game Game
		json.Unmarshal(file, &game)
		if game.Name != "Launcher" {
			games = append(games, game)
		} else {
			launcher = game
		}
	}

	return launcher, games
}
