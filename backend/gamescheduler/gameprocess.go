package gamescheduler

import (
	"log"
	"os/exec"

	. "github.com/s111/bachelor/backend/gameparser"
)

type gameProcess struct {
	game Game

	done chan error
}

func (gp *gameProcess) start() {
	command := exec.Command(gp.game.Exec[0], gp.game.Exec[1:]...)

	log.Println("Starting:", gp.game.Name)

	err := command.Start()

	if err != nil {
		log.Println("Done:", gp.game.Name+". Reason:", err)

		return
	}

	go func() {
		gp.done <- command.Wait()
	}()

	select {
	case err := <-gp.done:
		errKill := command.Process.Kill()

		// If there was an error it means the process was already killed
		if errKill == nil {
			// Wait for process to be killed
			<-gp.done
		}

		log.Println("Done:", gp.game.Name+". Reason:", err)
	}
}

func (gp *gameProcess) stop() {
	select {
	case gp.done <- nil:
	// Already stopped
	default:
	}

	log.Println("Stopping:", gp.game.Name)
}
