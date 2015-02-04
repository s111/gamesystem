package main

import (
	"flag"
	"io/ioutil"
	"log"
	"net/http"
	"path/filepath"
	"strings"
)

const version = "1.0"

var addr = flag.String("addr", ":3001", "http service address")
var debug = flag.Bool("debug", true, "debug")
var showVersion = flag.Bool("version", false, "show version number")

var scheduler = NewGameScheduler(parseGames())

func init() {
	flag.Parse()

	if *showVersion {
		log.Println("Version", version)
	}

	if !*debug {
		log.SetOutput(ioutil.Discard)
	}
}

func main() {
	for _, game := range scheduler.games {
		serveController(game)
	}

	go h.run()

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
