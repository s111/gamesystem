package gamescheduler

import (
	"log"
	"os/exec"

	. "github.com/s111/bachelor/backend/gameparser"
)

type gameProcess struct {
	game    Game
	command *exec.Cmd
}

func (gp *gameProcess) start() {
	gp.command = exec.Command(gp.game.Exec[0], gp.game.Exec[1:]...)

	log.Println("Starting:", gp.game.Name)

	err := gp.command.Run()

	log.Println("Done:", gp.game.Name+". Reason:", err)
}

func (gp *gameProcess) stop() {
	log.Println("Stopping:", gp.game.Name)

	gp.command.Process.Kill()
}
