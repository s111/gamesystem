package main

import (
	"flag"
	"io/ioutil"
	"log"
	"net/http"

	"github.com/s111/bachelor/backend/hub"
)

var addr = flag.String("addr", ":3001", "http service address")
var debug = flag.Bool("debug", true, "debug")

func init() {
	flag.Parse()

	if !*debug {
		log.SetOutput(ioutil.Discard)
	}
}

func main() {
	go hub.Run()

	http.HandleFunc("/ws", hub.ServeWs)

	err := http.ListenAndServe(*addr, nil)

	if err != nil {
		log.Fatal("ListenAndServe: ", err)
	}
}
