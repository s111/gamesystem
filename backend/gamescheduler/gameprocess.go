package gamescheduler

import (
	"log"
	"os/exec"
)

type gameProcess struct {
	name string
	exec []string

	done chan error
}

func (gp *gameProcess) start() {
	command := exec.Command(gp.exec[0], gp.exec[1:]...)

	err := command.Start()
	log.Println("Starting:", gp.name)

	if err != nil {
		log.Println("Done:", gp.name+". Reason:", err)

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

		log.Println("Done:", gp.name+". Reason:", err)
	}
}

func (gp *gameProcess) stop() {
	select {
	case gp.done <- nil:
	// Already stopped
	default:
	}

	log.Println("Stopping:", gp.name)
}
