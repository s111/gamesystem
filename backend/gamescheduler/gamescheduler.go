package gamescheduler

import . "github.com/s111/bachelor/backend/gameparser"

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
	const launcher = "Launcher"

	go gs.start(launcher)

	for {
		select {
		case g := <-gs.startGame:
			g.start()

			gs.ready = false

			if gs.currentProcess.game.Name == launcher {
				return
			}

			select {
			case g := <-gs.startGame:
				go func() { gs.startGame <- g }()
			default:
				go gs.start(launcher)
			}
		}
	}
}

func (gs *gameScheduler) getCurrentGameName() string {
	return gs.currentProcess.game.Name
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

func GetCurrentGameName() string {
	return gs.getCurrentGameName()
}
