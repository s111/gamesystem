package main

import (
	"flag"
	"io/ioutil"
	"log"
	"net/http"
	"sync"
	"time"

	gs "github.com/s111/bachelor/backend/gamescheduler"
	"github.com/s111/bachelor/backend/hub"
)

const (
	launcher = "Launcher"

	gameClient = "game"

	actionGetIP       = "get ip"
	actionRedirect    = "redirect"
	actionList        = "list"
	actionStart       = "start"
	actionDescription = "get description"
	actionPlayers     = "get players"
)

var addr = flag.String("addr", ":3001", "http service address")
var scheduler = flag.Bool("scheduler", true, "enable/disable the scheduler")
var debug = flag.Bool("debug", true, "debug")

var wg sync.WaitGroup

func main() {
	flag.Parse()

	if !*debug {
		log.SetOutput(ioutil.Discard)
	}

	wg.Add(1)

	hub.SetTimeout(time.Second * 10)
	go hub.Run()
	go gs.Run()

	setupSchedulerCallbacks()

	games := getAvailableGames()
	names := getListOfGameNames(games)
	addGamesToScheduler(games)

	if *scheduler {
		gs.Start(launcher)
	}

	setupHubEventHandlers()
	setupHubMessageHandlers(games, names)

	setupHTTPHandlers(games)

	go func() {
		err := http.ListenAndServe(*addr, nil)

		if err != nil {
			log.Fatal("ListenAndServe: ", err)
		}

		wg.Done()
	}()

	wg.Wait()
}
