package gamescheduler

import (
	"log"

	. "github.com/s111/bachelor/backend/gameparser"
)

const Launcher = "Launcher"

var gs = gameScheduler{
	startGame:      make(chan *gameProcess),
	currentProcess: &gameProcess{},
	ready:          false,
}

type gameScheduler struct {
	games          map[string]gameProcess
	startGame      chan *gameProcess
	currentProcess *gameProcess
	ready          bool
}

func (gs *gameScheduler) start(name string) {
	if game, ok := gs.games[name]; ok {
		lastGame := gs.currentProcess
		gs.currentProcess = &game

		if lastGame.game.Name != "" {
			lastGame.stop()
		}

		gs.startGame <- &game
	}
}

func (gs *gameScheduler) run() {
	if len(gs.games) == 0 {
		log.Println("no games found")

		return
	}

	go gs.start(Launcher)

	for {
		select {
		case g := <-gs.startGame:
			g.start()

			gs.ready = false

			if gs.currentProcess.game.Name == Launcher {
				return
			}

			select {
			case g := <-gs.startGame:
				go func() { gs.startGame <- g }()
			default:
				go gs.start(Launcher)
			}
		}
	}
}

func (gs *gameScheduler) getCurrentGameName() string {
	return gs.currentProcess.game.Name
}

func GetCurrentGameName() string {
	return gs.getCurrentGameName()
}

func Start(name string) {
	gs.start(name)
}

func Run(games map[string]Game) {
	g := make(map[string]gameProcess)

	for _, game := range games {
		g[game.Name] = gameProcess{
			game: game,
		}
	}

	gs.games = g
	gs.run()
}
