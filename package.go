package main

import (
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
)

var gamesDir = "games"

var nativesDir, libDir, controllerDir, newControllerDir string

func main() {
	rootDir, _ := os.Getwd()

	newGamesDir := filepath.Join(rootDir, "/backend/games")

	games, _ := ioutil.ReadDir(gamesDir)

	for _, game := range games {
		gameName := game.Name()
		gamePath := filepath.Join(gamesDir, gameName)
		gamePath = filepath.Join(rootDir, gamePath) // temp fix

		json := filepath.Join(gamePath, "game.json")
		gameDir := filepath.Join(gamePath, "/game")
		controllerDir = filepath.Join(gamePath, "controller")
		targetDir := filepath.Join(gameDir, "target")
		nativesDir = filepath.Join(targetDir, "natives")

		newGameDir := filepath.Join(newGamesDir, gameName)
		newJson := filepath.Join(newGameDir, "game.json")
		binDir := filepath.Join(newGameDir, "bin")
		libDir = filepath.Join(newGameDir, "lib")
		newControllerDir = filepath.Join(newGameDir, "controller")

		os.MkdirAll(binDir, 0777)

		os.Chdir(gameDir)

		runCommand("mvn clean compile assembly:single")

		os.Chdir(rootDir)

		jarName := getJarName(filepath.Join(gameDir))
		jar := filepath.Join(targetDir, jarName)
		newJar := filepath.Join(binDir, gameName+".jar")

		os.Symlink(json, newJson)
		os.Symlink(jar, newJar)

		os.Symlink(nativesDir, libDir)
		os.Symlink(controllerDir, newControllerDir)
	}
}

func runCommand(cmd string) {
	parts := strings.Fields(cmd)
	head := parts[0]
	parts = parts[1:len(parts)]

	out, err := exec.Command(head, parts...).Output()

	if err != nil {
		log.Printf("Could not execute command: %v\n", cmd)
		return
	}

	fmt.Printf("%s", out)
}

func getJarName(path string) string {
	jarPath := filepath.Join(path, "target")
	var jarName string

	d, err := os.Open(jarPath)
	defer d.Close()
	if err != nil {
		fmt.Println(err)
		os.Exit(1)
	}

	files, err := d.Readdir(-1)
	if err != nil {
		fmt.Println(err)
		os.Exit(1)
	}

	for _, file := range files {
		if file.Mode().IsRegular() {
			if filepath.Ext(file.Name()) == ".jar" {
				jarName = file.Name()
			}
		}
	}
	return jarName
}
