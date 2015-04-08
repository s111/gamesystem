package main

import (
	"encoding/json"
	"flag"
	"io/ioutil"
	"log"
	"net/http"
	"path/filepath"
	"strings"
	"sync"
	"time"

	. "github.com/s111/bachelor/backend/gameparser"
	gs "github.com/s111/bachelor/backend/gamescheduler"
	"github.com/s111/bachelor/backend/hub"
)

const (
	launcher = "Launcher"

	gameClient = "game"

	actionRedirect    = "redirect"
	actionList        = "list"
	actionStart       = "start"
	actionDescription = "get description"
	actionPlayers     = "get players"
)

var addr = flag.String("addr", ":3001", "http service address")
var scheduler = flag.Bool("scheduler", true, "enable/disable the scheduler")
var debug = flag.Bool("debug", true, "debug")

func init() {
	flag.Parse()

	if !*debug {
		log.SetOutput(ioutil.Discard)
	}
}

func main() {
	var wg sync.WaitGroup
	wg.Add(1)

	hub.SetTimeout(time.Second * 10)
	go hub.Run()
	go gs.Run()

	gs.OnStart(func(name string) {
		hub.Send(hub.MessageOut{
			From:   gameClient,
			To:     hub.Broadcast,
			Action: actionRedirect,
			Data:   name,
		})
	})

	gs.OnStop(func(name string, current string) {
		// If no new game has started
		if name == current {
			if name != launcher {
				gs.Start(launcher)
			} else {
				gs.Quit()
				wg.Done()
			}
		}
	})

	gp := GameParser{
		Games: make(map[string]Game),
	}
	gp.Parse()

	var games []string

	for name, game := range gp.Games {
		cmd, err := game.GetCmd()

		if err != nil {
			continue
		}

		gs.Add(name, cmd)

		if name == launcher {
			continue
		}

		games = append(games, name)
	}

	if *scheduler {
		gs.Start(launcher)
	}

	hub.AddEventHandler(hub.EventAdd, func(id string) {
		if id == gameClient {
			return
		}

		hub.Send(hub.MessageOut{
			To:     gameClient,
			Action: hub.ActionAdd,
			Data:   id,
		})

		hub.Send(hub.MessageOut{
			From:   gameClient,
			To:     id,
			Action: actionRedirect,
			Data:   gs.GetCurrent(),
		})
	})

	hub.AddEventHandler(hub.EventResume, func(id string) {
		if id == gameClient {
			return
		}

		hub.Send(hub.MessageOut{
			From:   gameClient,
			To:     id,
			Action: actionRedirect,
			Data:   gs.GetCurrent(),
		})
	})

	hub.AddEventHandler(hub.EventDrop, func(id string) {
		if id == gameClient {
			return
		}

		hub.Send(hub.MessageOut{
			To:     gameClient,
			Action: hub.ActionDrop,
			Data:   id,
		})
	})

	hub.AddMessageHandler(actionList, func(m hub.MessageIn) {
		hub.Send(hub.MessageOut{
			To:     m.From,
			Action: actionList,
			Data:   games,
		})
	})

	hub.AddMessageHandler(actionStart, func(m hub.MessageIn) {
		var data string

		err := json.Unmarshal(m.Data, &data)

		if err != nil {
			return
		}

		gs.Start(data)
	})

	hub.AddMessageHandler(actionDescription, func(m hub.MessageIn) {
		var data string

		err := json.Unmarshal(m.Data, &data)

		if err != nil {
			return
		}

		description := ""

		if game, ok := gp.Games[data]; ok {
			if len(game.Description) > 0 {
				description = game.Description
			}
		}

		hub.Send(hub.MessageOut{
			To:     m.From,
			Action: actionDescription,
			Data:   description,
		})
	})

	hub.AddMessageHandler(actionPlayers, func(m hub.MessageIn) {
		var data string

		err := json.Unmarshal(m.Data, &data)

		if err != nil {
			return
		}

		players := 0

		if game, ok := gp.Games[data]; ok {
			if game.Players > 0 {
				players = game.Players
			}
		}

		hub.Send(hub.MessageOut{
			To:     m.From,
			Action: actionPlayers,
			Data:   players,
		})
	})

	for _, game := range gp.Games {
		serveController(game)
	}

	http.HandleFunc("/ws", hub.ServeWs)
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		redirectToController(w, r)
	})
	http.HandleFunc("/hub.js", func(w http.ResponseWriter, r *http.Request) {
		http.ServeFile(w, r, "hub.js")
	})

	go func() {
		err := http.ListenAndServe(*addr, nil)

		if err != nil {
			log.Fatal("ListenAndServe: ", err)
		}
	}()

	wg.Wait()
}

func serveController(game Game) {
	controllerPath := filepath.Join(GamesDir, strings.ToLower(game.Name), ControllerDir)
	prefix := strings.ToLower(game.Name)
	http.Handle("/"+prefix+"/", http.StripPrefix("/"+prefix+"/", http.FileServer(http.Dir(controllerPath))))
}

func redirectToController(w http.ResponseWriter, r *http.Request) {
	currentGame := gs.GetCurrent()

	if currentGame != "" {
		http.Redirect(w, r, "/"+strings.ToLower(currentGame), http.StatusFound)
	}
}
