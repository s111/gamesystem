package gamescheduler

import "sync"

var gs = gamescheduler{
	games: make(map[string]gameProcess),

	add:   make(chan gameProcess),
	start: make(chan string),
	quit:  make(chan bool),
}

type gamescheduler struct {
	games map[string]gameProcess

	add   chan gameProcess
	start chan string

	cLock   sync.RWMutex
	current *gameProcess

	onStart func(string)
	onStop  func(string, string)

	quit chan bool
}

func (gs *gamescheduler) run() {
	for {
		select {
		case gp := <-gs.add:
			gs.games[gp.name] = gp
		case name := <-gs.start:
			if gp, ok := gs.games[name]; ok {
				gs.cLock.Lock()
				last := gs.current
				gs.current = &gp
				gs.cLock.Unlock()

				if last != nil {
					last.stop()
				}

				go func() {
					if gs.onStart != nil {
						gs.onStart(name)
					}

					gp.start()

					gs.cLock.RLock()
					current := gs.current.name
					gs.cLock.RUnlock()

					if gs.onStop != nil {
						gs.onStop(name, current)
					}
				}()
			}
		case <-gs.quit:
			gs.current.stop()

			return
		}
	}
}

// Run starts the scheduler.
// No games will be run until they are added with Add and run with Start.
func Run() {
	gs.run()
}

// Add adds a new game to the scheduler.
func Add(name string, exec []string) {
	gp := gameProcess{
		name: name,
		exec: exec,

		done: make(chan error),
	}

	gs.add <- gp
}

// Start will stop the current game and start the one provided, if it exists.
func Start(name string) {
	gs.start <- name
}

// OnStart allows you to set a callback function which will be run when a game is started.
func OnStart(cb func(name string)) {
	gs.onStart = cb
}

// OnStop allows you to set a callback function which will be run when a game is stopped.
func OnStop(cb func(name string, current string)) {
	gs.onStop = cb
}

// GetCurrent returns the name of the current running game.
func GetCurrent() string {
	gs.cLock.RLock()
	defer gs.cLock.RUnlock()

	return gs.current.name
}

// Quit stops the scheduler
func Quit() {
	gs.quit <- true
}
