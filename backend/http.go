package backend

import (
	"net/http"
	"path/filepath"
	"strings"

	gp "github.com/s111/gamesystem/gameparser"
	gs "github.com/s111/gamesystem/gamescheduler"
	"github.com/s111/gamesystem/hub"
)

func setupHTTPHandlers(games map[string]gp.Game) {
	for _, game := range games {
		serveController(game)

		func(name string) {
			http.HandleFunc("/img/"+name+".png", func(w http.ResponseWriter, r *http.Request) {
				http.ServeFile(w, r, filepath.Join(gp.GamesDir, name, "screenshot.png"))
			})
		}(strings.ToLower(game.Name))
	}

	http.HandleFunc("/ws", hub.ServeWs)

	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		redirectToController(w, r)
	})

	http.HandleFunc("/hub.js", func(w http.ResponseWriter, r *http.Request) {
		http.ServeFile(w, r, "hub.js")
	})

	http.HandleFunc("/jquery.js", func(w http.ResponseWriter, r *http.Request) {
		http.ServeFile(w, r, "jquery.js")
	})

	http.HandleFunc("/phaser.js", func(w http.ResponseWriter, r *http.Request) {
		http.ServeFile(w, r, "phaser.js")
	})

}

func serveController(game gp.Game) {
	controllerPath := filepath.Join(gp.GamesDir, strings.ToLower(game.Name), gp.ControllerDir)
	prefix := strings.ToLower(game.Name)
	http.Handle("/"+prefix+"/", http.StripPrefix("/"+prefix+"/", http.FileServer(http.Dir(controllerPath))))
}

func redirectToController(w http.ResponseWriter, r *http.Request) {
	currentGame := gs.GetCurrent()

	if currentGame != "" {
		http.Redirect(w, r, "/"+strings.ToLower(currentGame), http.StatusFound)
	}
}
