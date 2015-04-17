package main

import (
	gp "github.com/s111/bachelor/backend/gameparser"
	gs "github.com/s111/bachelor/backend/gamescheduler"
)

func getAvailableGames() map[string]gp.Game {
	parser := gp.GameParser{
		Games: make(map[string]gp.Game),
	}
	parser.Parse()

	return parser.Games
}

func getListOfGameNames(games map[string]gp.Game) []string {
	var names []string

	for name := range games {
		if name == launcher {
			continue
		}

		names = append(names, name)
	}

	return names
}

func addGamesToScheduler(games map[string]gp.Game) {
	for name, game := range games {
		cmd, err := game.GetCmd()

		if err != nil {
			continue
		}

		gs.Add(name, cmd)
	}

}
