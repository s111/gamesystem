package backend

import (
	gs "github.com/s111/gamesystem/gamescheduler"
	"github.com/s111/gamesystem/hub"
)

func setupSchedulerCallbacks() {
	gs.OnStart(func(name string) {
		hub.Send(hub.MessageOut{
			From:   gameClient,
			To:     hub.Broadcast,
			Action: actionRedirect,
			Data:   name,
		})
	})

	gs.OnStop(func(name string, current string) {
		// If no new game has started
		if name == current {
			if name != launcher {
				gs.Start(launcher)
			} else {
				gs.Quit()
				wg.Done()
			}
		}
	})
}
