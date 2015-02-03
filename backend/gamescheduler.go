package main

type GameScheduler struct {
	games       map[string]Game
	startGame   chan *Game
	currentGame *Game
}

func NewGameScheduler(games []Game) GameScheduler {
	g := make(map[string]Game)

	for _, game := range games {
		g[game.Name] = game
	}

	gs := GameScheduler{
		games:       g,
		startGame:   make(chan *Game),
		currentGame: &Game{},
	}

	return gs
}

func (gs *GameScheduler) start(name string) {
	if game, ok := gs.games[name]; ok {
		lastGame := gs.currentGame
		gs.currentGame = &game

		if lastGame.Name != "" {
			lastGame.stop()
		}

		gs.startGame <- &game
	}
}

func (gs *GameScheduler) run() {
	const launcher = "Launcher"

	go gs.start(launcher)

	for {
		select {
		case g := <-gs.startGame:
			g.start()

			if gs.currentGame.Name == launcher {
				return
			}

			select {
			case g := <-gs.startGame:
				gs.startGame <- g
			default:
				go gs.start(launcher)
			}
		}
	}
}
