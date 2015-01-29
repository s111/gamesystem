package main

import "os/exec"

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
	g.Command.Run()
}

func (g *Game) stop() {
	g.Command.Process.Kill()
}
