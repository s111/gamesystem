// Package backend uses the hub, gamescheduler and gameparser to create a game system.
package backend

import (
	"flag"
	"io/ioutil"
	"log"
	"net/http"
	"sync"
	"time"

	gs "github.com/s111/gamesystem/gamescheduler"
	"github.com/s111/gamesystem/hub"
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
var game = flag.String("game", launcher, "the first game to be started")

var wg sync.WaitGroup

// Run starts the back end.
func Run() {
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

	exists := false

	for _, g := range names {
		if g == *game {
			exists = true
		}
	}

	if *scheduler {
		if exists {
			gs.Start(*game)
		} else {
			log.Fatal("Game doesn't exist: ", *game)

			wg.Done()
		}
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
