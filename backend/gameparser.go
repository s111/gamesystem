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

func parseGames() []Game {
	var games []Game

	names, _ := ioutil.ReadDir(gamesDir)

	for _, name := range names {
		gamePath := filepath.Join(gamesDir, name.Name())
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
		games = append(games, game)
	}

	return games
}
