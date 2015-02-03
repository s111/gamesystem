package main

import (
	"flag"
	"log"
	"net/http"
	"path/filepath"
	"strings"
)

var addr = flag.String("addr", ":3001", "http service address")

var scheduler = NewGameScheduler(parseGames())

func main() {
	flag.Parse()

	for _, game := range scheduler.games {
		serveController(game)
	}

	http.HandleFunc("/ws", serverWs)
	http.HandleFunc("/", redirectToController)

	go func() {
		err := http.ListenAndServe(*addr, nil)

		if err != nil {
			log.Fatal("ListenAndServe: ", err)
		}
	}()

	scheduler.run()
}

func serveController(game Game) {
	controllerPath := filepath.Join(gamesDir, strings.ToLower(game.Name), controllerDir)
	prefix := strings.ToLower(game.Name)

	http.Handle("/"+prefix+"/", http.StripPrefix("/"+prefix+"/", http.FileServer(http.Dir(controllerPath))))
}

func redirectToController(w http.ResponseWriter, r *http.Request) {
	if scheduler.currentGame.Name != "" {
		http.Redirect(w, r, "/"+strings.ToLower(scheduler.currentGame.Name), http.StatusFound)
	}
}
