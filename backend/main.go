package main

import (
	"encoding/json"
	"errors"
	"flag"
	"io/ioutil"
	"log"
	"net"
	"net/http"
	"path/filepath"
	"strings"
	"sync"
	"time"

	. "github.com/s111/bachelor/backend/gameparser"
	gs "github.com/s111/bachelor/backend/gamescheduler"
	"github.com/s111/bachelor/backend/hub"
)

const (
	launcher = "Launcher"

	gameClient = "game"

	actionGetIp       = "get ip"
	actionRedirect    = "redirect"
	actionList        = "list"
	actionStart       = "start"
	actionDescription = "get description"
	actionPlayers     = "get players"
)

var addr = flag.String("addr", ":3001", "http service address")
var scheduler = flag.Bool("scheduler", true, "enable/disable the scheduler")
var debug = flag.Bool("debug", true, "debug")

func init() {
	flag.Parse()

	if !*debug {
		log.SetOutput(ioutil.Discard)
	}
}

func main() {
	var wg sync.WaitGroup
	wg.Add(1)

	hub.SetTimeout(time.Second * 10)
	go hub.Run()
	go gs.Run()

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

	gp := GameParser{
		Games: make(map[string]Game),
	}
	gp.Parse()

	var games []string

	for name, game := range gp.Games {
		cmd, err := game.GetCmd()

		if err != nil {
			continue
		}

		gs.Add(name, cmd)

		if name == launcher {
			continue
		}

		games = append(games, name)
	}

	if *scheduler {
		gs.Start(launcher)
	}

	hub.AddEventHandler(hub.EventAdd, func(id string) {
		if id == gameClient {
			return
		}

		hub.Send(hub.MessageOut{
			To:     gameClient,
			Action: hub.EventAdd,
			Data:   id,
		})

		hub.Send(hub.MessageOut{
			From:   gameClient,
			To:     id,
			Action: actionRedirect,
			Data:   gs.GetCurrent(),
		})
	})

	hub.AddEventHandler(hub.EventResume, func(id string) {
		if id == gameClient {
			return
		}

		hub.Send(hub.MessageOut{
			From:   gameClient,
			To:     id,
			Action: actionRedirect,
			Data:   gs.GetCurrent(),
		})
	})

	hub.AddEventHandler(hub.EventDrop, func(id string) {
		if id == gameClient {
			return
		}

		hub.Send(hub.MessageOut{
			To:     gameClient,
			Action: hub.EventDrop,
			Data:   id,
		})
	})

	hub.AddEventHandler(hub.EventUsernameChange, func(id string) {
		if id == gameClient {
			return
		}

		hub.Send(hub.MessageOut{
			To:     gameClient,
			Action: hub.ActionGetUsername,
			Data:   id,
		})
	})

	hub.AddMessageHandler(actionGetIp, func(m hub.MessageIn) {
		host, port, err := net.SplitHostPort(*addr)

		if err != nil {
			log.Println("get ip:", err)

			return
		}

		if host == "" || host == "localhost" {
			host, err = externalIP()

			if err != nil {
				log.Println("get ip:", err)

				return
			}
		}

		displayAddr := net.JoinHostPort(host, port)

		hub.Send(hub.MessageOut{
			To:     m.From,
			Action: actionGetIp,
			Data:   displayAddr,
		})

	})

	hub.AddMessageHandler(actionList, func(m hub.MessageIn) {
		hub.Send(hub.MessageOut{
			To:     m.From,
			Action: actionList,
			Data:   games,
		})
	})

	hub.AddMessageHandler(actionStart, func(m hub.MessageIn) {
		var data string

		err := json.Unmarshal(m.Data, &data)

		if err != nil {
			return
		}

		gs.Start(data)
	})

	hub.AddMessageHandler(actionDescription, func(m hub.MessageIn) {
		var data string

		err := json.Unmarshal(m.Data, &data)

		if err != nil {
			return
		}

		var description string

		if game, ok := gp.Games[data]; ok {
			description = game.Description
		}

		hub.Send(hub.MessageOut{
			To:     m.From,
			Action: actionDescription,
			Data:   description,
		})
	})

	hub.AddMessageHandler(actionPlayers, func(m hub.MessageIn) {
		var data string

		err := json.Unmarshal(m.Data, &data)

		if err != nil {
			return
		}

		var players int

		if game, ok := gp.Games[data]; ok {
			players = game.Players
		}

		hub.Send(hub.MessageOut{
			To:     m.From,
			Action: actionPlayers,
			Data:   players,
		})
	})

	for _, game := range gp.Games {
		serveController(game)

		func(name string) {
			http.HandleFunc("/img/"+name+".png", func(w http.ResponseWriter, r *http.Request) {
				http.ServeFile(w, r, filepath.Join(GamesDir, name, "screenshot.png"))
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

	go func() {
		err := http.ListenAndServe(*addr, nil)

		if err != nil {
			log.Fatal("ListenAndServe: ", err)
		}
	}()

	wg.Wait()
}

func serveController(game Game) {
	controllerPath := filepath.Join(GamesDir, strings.ToLower(game.Name), ControllerDir)
	prefix := strings.ToLower(game.Name)
	http.Handle("/"+prefix+"/", http.StripPrefix("/"+prefix+"/", http.FileServer(http.Dir(controllerPath))))
}

func redirectToController(w http.ResponseWriter, r *http.Request) {
	currentGame := gs.GetCurrent()

	if currentGame != "" {
		http.Redirect(w, r, "/"+strings.ToLower(currentGame), http.StatusFound)
	}
}

// taken from https://code.google.com/p/whispering-gophers/source/browse/util/helper.go
func externalIP() (string, error) {
	ifaces, err := net.Interfaces()
	if err != nil {
		return "", err
	}
	for _, iface := range ifaces {
		if iface.Flags&net.FlagUp == 0 {
			continue // interface down
		}
		if iface.Flags&net.FlagLoopback != 0 {
			continue // loopback interface
		}
		addrs, err := iface.Addrs()
		if err != nil {
			return "", err
		}
		for _, addr := range addrs {
			var ip net.IP
			switch v := addr.(type) {
			case *net.IPNet:
				ip = v.IP
			case *net.IPAddr:
				ip = v.IP
			}
			if ip == nil || ip.IsLoopback() {
				continue
			}
			ip = ip.To4()
			if ip == nil {
				continue // not an ipv4 address
			}
			return ip.String(), nil
		}
	}
	return "", errors.New("are you connected to the network?")
}
