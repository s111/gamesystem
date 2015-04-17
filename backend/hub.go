package main

import (
	"encoding/json"
	"log"
	"net"

	gp "github.com/s111/bachelor/backend/gameparser"
	gs "github.com/s111/bachelor/backend/gamescheduler"
	"github.com/s111/bachelor/backend/hub"
)

func setupHubEventHandlers() {
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
}

func setupHubMessageHandlers(games map[string]gp.Game, names []string) {
	hub.AddMessageHandler(actionGetIP, func(m hub.MessageIn) {
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
			Action: actionGetIP,
			Data:   displayAddr,
		})

	})

	hub.AddMessageHandler(actionList, func(m hub.MessageIn) {
		hub.Send(hub.MessageOut{
			To:     m.From,
			Action: actionList,
			Data:   names,
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

		if game, ok := games[data]; ok {
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

		if game, ok := games[data]; ok {
			players = game.Players
		}

		hub.Send(hub.MessageOut{
			To:     m.From,
			Action: actionPlayers,
			Data:   players,
		})
	})
}
