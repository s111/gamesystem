package main

type GameScheduler struct {
	games       []Game
	launcher    Game
	currentGame Game
}

func (gs *GameScheduler) startGame(id int) {
	gs.currentGame = gs.games[id]
	gs.currentGame.start()
}
