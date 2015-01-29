package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"strings"

	"github.com/codegangsta/martini"
	"golang.org/x/net/websocket"
)

type Game struct {
	Name string
	Exec []string
}

const path = "games"

var server *martini.ClassicMartini
var games []Game

func main() {
	http.HandleFunc("/ws",
		func(w http.ResponseWriter, req *http.Request) {
			s := websocket.Server{Handler: websocket.Handler(wsHandler)}
			s.ServeHTTP(w, req)
		})

	go func() {
		if err := http.ListenAndServe(":3001", nil); err != nil {
			panic(err.Error())
		}
	}()

	server = martini.Classic()

	parseGames()
	listGames()

	go func() {
		server.Run()
	}()

	for {
		selectGame()
	}
}

func wsHandler(ws *websocket.Conn) {
	websocket.JSON.Send(ws, games)

	var data int

	for {
		err := websocket.JSON.Receive(ws, &data)

		if err != nil {
			if err.Error() == "EOF" {
				fmt.Println("Client closed connection")
			} else {
				fmt.Println("Dropping client: " + err.Error())
			}

			return
		}

		fmt.Printf("Receive: %s\n", data)

		startGame(games[data])
	}
}

func parseGames() {
	names, _ := ioutil.ReadDir(path)

	for _, name := range names {
		gamePath := filepath.Join(path, name.Name())
		filename := filepath.Join(gamePath, "game.json")

		if _, err := os.Stat(filename); os.IsNotExist(err) {
			fmt.Println("Warning: Found directory without game.json:", filename)

			continue
		}

		file, _ := ioutil.ReadFile(filename)

		var game Game

		json.Unmarshal(file, &game)

		games = append(games, game)

		addController(game.Name, gamePath)
	}
}

func addController(gameName string, gamePath string) {
	controllerPath := filepath.Join(gamePath, "controller")
	prefix := strings.ToLower(gameName)

	server.Use(martini.Static(controllerPath, martini.StaticOptions{
		Prefix: prefix,
	}))
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
	err := exec.Command(game.Exec[0], game.Exec[1:]...).Run()

	if err != nil {
		fmt.Println(err.Error())
	}
}
