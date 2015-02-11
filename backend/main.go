package main

import (
	"flag"
	"io/ioutil"
	"log"
	"net/http"
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
	go h.run()

	http.HandleFunc("/ws", serverWs)

	err := http.ListenAndServe(*addr, nil)

	if err != nil {
		log.Fatal("ListenAndServe: ", err)
	}
}
