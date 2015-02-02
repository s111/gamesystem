package main

type GameScheduler struct {
	games       map[string]Game
	startGame   chan Game
	currentGame Game
}

func (gs *GameScheduler) startGame(name string) {
	if currentGame.Name != "" {
		return
	}

	if game, ok := games[name]; ok {
		startGame <- game
	}
}

func (gs *GameScheduler) run() {
	const launcher = "launcher"

	go gs.startGame(launcher)

	for {
		select {
		case g := <-startGame:
			gs.currentGame = g
			g.start()

			if gs.currentGame.Name != launcher {
				go func() {
					gs.currentGame = Game{}
					gs.startGame(launcher)
				}()
			} else {
				gs.currentGame = Game{}
			}
		}
	}
}
