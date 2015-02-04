package main

type GameScheduler struct {
	games       map[string]Game
	startGame   chan *Game
	currentGame *Game
	ready       bool
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
		ready:       false,
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

			gs.ready = false

			if gs.currentGame.Name == launcher {
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
