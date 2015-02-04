package main

import (
	"log"
	"os/exec"
)

type GameProcess interface {
	start()
	stop()
}

type Game struct {
	Name    string
	Exec    []string
	Command *exec.Cmd
}

func (g *Game) start() {
	g.Command = exec.Command(g.Exec[0], g.Exec[1:]...)

	log.Println("Starting:", g.Name)

	err := g.Command.Run()

	log.Println("Done:", g.Name+". Reason:", err)
}

func (g *Game) stop() {
	log.Println("Stopping:", g.Name)

	g.Command.Process.Kill()
}
