package main

import (
	"flag"
	"log"
	"net/http"
	"path/filepath"
	"strings"
)

var addr = flag.String("addr", ":3001", "http service address")

var launcher, games = parseGames()
var currentGame Game

func main() {
	flag.Parse()

	serveController(launcher)

	for _, game := range games {
		serveController(game)
	}

	http.HandleFunc("/ws", serverWs)
	http.HandleFunc("/", redirectToController)

	err := http.ListenAndServe(*addr, nil)

	if err != nil {
		log.Fatal("ListenAndServe: ", err)
	}
}

func serveController(game Game) {
	controllerPath := filepath.Join(gamesDir, strings.ToLower(game.Name), controllerDir)
	prefix := strings.ToLower(game.Name)

	http.Handle("/"+prefix+"/", http.StripPrefix("/"+prefix+"/", http.FileServer(http.Dir(controllerPath))))
}

func redirectToController(w http.ResponseWriter, r *http.Request) {
	if currentGame.Name != "" {
		http.Redirect(w, r, "/"+strings.ToLower(currentGame.Name), http.StatusFound)
	} else {
		http.Redirect(w, r, "/"+strings.ToLower("launcher"), http.StatusFound)
	}
}
