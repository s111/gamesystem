package main

import (
	"encoding/json"
	"flag"
	"io/ioutil"
	"log"
	"net/http"
	"path/filepath"
	"strings"
	"time"

	. "github.com/s111/bachelor/backend/gameparser"
	gs "github.com/s111/bachelor/backend/gamescheduler"
	"github.com/s111/bachelor/backend/hub"
)

var addr = flag.String("addr", ":3001", "http service address")
var debug = flag.Bool("debug", true, "debug")

func init() {
	flag.Parse()

	if !*debug {
		log.SetOutput(ioutil.Discard)
	}
}

func main() {
	hub.SetTimeout(time.Second * 10)
	go hub.Run()

	gp := GameParser{
		Games: make(map[string]Game),
	}
	gp.Parse()

	var games []string

	for g := range gp.Games {
		if g == gs.Launcher {
			continue
		}

		games = append(games, g)
	}

	hub.AddMessageHandler(hub.ActionList, func(m hub.MessageIn) {
		hub.Send(hub.MessageOut{
			To:     m.From,
			Action: hub.ActionList,
			Data:   games,
		})
	})

	hub.AddMessageHandler(hub.ActionStart, func(m hub.MessageIn) {
		var data string

		err := json.Unmarshal(m.Data, &data)

		if err != nil {
			return
		}

		gs.Start(data)
	})

	for _, game := range gp.Games {
		serveController(game)
	}

	http.HandleFunc("/ws", hub.ServeWs)

	go func() {
		err := http.ListenAndServe(*addr, nil)

		if err != nil {
			log.Fatal("ListenAndServe: ", err)
		}
	}()

	gs.Run(gp.Games)
}

func serveController(game Game) {
	controllerPath := filepath.Join(GamesDir, strings.ToLower(game.Name), ControllerDir)
	prefix := strings.ToLower(game.Name)
	http.Handle("/"+prefix+"/", http.StripPrefix("/"+prefix+"/", http.FileServer(http.Dir(controllerPath))))
}

func redirectToController(w http.ResponseWriter, r *http.Request) {
	currentGame := gs.GetCurrentGameName()

	if currentGame != "" {
		http.Redirect(w, r, "/"+strings.ToLower(currentGame), http.StatusFound)
	}
}
