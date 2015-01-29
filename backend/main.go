package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
	"path/filepath"
	"strings"
)

const gamesDir = "games"
const controllerDir = "controller"

var games []Game

var currentGame Game

func main() {
	parseGames()
	setUpHttpHandlers()
	listGames()

	if err := http.ListenAndServe(":3001", nil); err != nil {
		panic(err.Error())
	}

}

func setUpHttpHandlers() {
	http.HandleFunc("/",
		func(w http.ResponseWriter, r *http.Request) {
			if currentGame.Name != "" {
				http.Redirect(w, r, "/"+strings.ToLower(currentGame.Name), http.StatusFound)
			} else {
				http.Redirect(w, r, "/"+strings.ToLower("launcher"), http.StatusFound)
			}
		})

	for _, game := range games {
		controllerPath := filepath.Join(gamesDir, strings.ToLower(game.Name), controllerDir)
		prefix := strings.ToLower(game.Name)

		http.Handle("/"+prefix+"/", http.StripPrefix("/"+prefix+"/", http.FileServer(http.Dir(controllerPath))))
	}

	server := NewServer("/ws")
	go server.Listen()
}

func parseGames() {
	names, _ := ioutil.ReadDir(gamesDir)

	for _, name := range names {
		gamePath := filepath.Join(gamesDir, name.Name())
		filename := filepath.Join(gamePath, "game.json")

		if _, err := os.Stat(filename); os.IsNotExist(err) {
			fmt.Println("Warning: Found directory without game.json:", filename)

			continue
		}

		file, _ := ioutil.ReadFile(filename)

		var game Game
		json.Unmarshal(file, &game)
		games = append(games, game)
	}
}

func listGames() {
	fmt.Println("Availabe games:")

	for i, game := range games {
		if strings.ToLower(game.Name) != "launcher" {
			fmt.Printf("\t\t%v. %v\n", i+1, game.Name)
		}
	}
}
