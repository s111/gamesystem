package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"os/exec"
	"path/filepath"
)

type Game struct {
	Name string
	Exec []string
}

const path = "games"

var games []Game

func main() {
	parseGames()
	listGames()

	for {
		selectGame()
	}
}

func parseGames() {
	names, _ := ioutil.ReadDir(path)

	for _, name := range names {
		filename := filepath.Join(path, name.Name(), "game.json")

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
		fmt.Printf("\t\t%v. %v\n", i+1, game.Name)
	}
}

func selectGame() {
	var i int

	_, err := fmt.Scanf("%d", &i)

	i--

	if err != nil || i < 0 || i >= len(games) {
		return
	}

	startGame(games[i])
}

func startGame(game Game) {
	exec.Command(game.Exec[0], game.Exec[1:]...).Run()

}
