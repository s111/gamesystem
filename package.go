package main

import (
	"flag"
	"io/ioutil"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
)

var (
	help = flag.Bool(
		"help",
		false,
		"Show usage help",
	)
	gameFlag = flag.String(
		"game",
		"",
		"Specify which game to compile",
	)

	gamesDir    string
	newGamesDir string
)

func Usage() {
	flag.PrintDefaults()
}

func main() {
	flag.Usage = Usage
	flag.Parse()

	if *help {
		flag.Usage()
		os.Exit(0)
	}

	rootDir, _ := os.Getwd()

	gamesDir = filepath.Join(rootDir, "games")
	newGamesDir = filepath.Join(rootDir, "backend", "games")

	games, _ := ioutil.ReadDir(gamesDir)

	for _, game := range games {
		gameName := game.Name()

		gamePath := filepath.Join(gamesDir, gameName)
		gameDir := filepath.Join(gamePath, "game")

		if *gameFlag == gameName || *gameFlag == "" {
			os.Chdir(gameDir)
			compileGame(gameName)
		}

		symlinkGame(gameName)
	}

	log.Println("*** PACKAGING COMPLETE ***\n")
}

func symlinkGame(gameName string) {
	gamePath := filepath.Join(gamesDir, gameName)

	json := filepath.Join(gamePath, "game.json")
	gameDir := filepath.Join(gamePath, "game")
	controllerDir := filepath.Join(gamePath, "controller")
	targetDir := filepath.Join(gameDir, "target")
	nativesDir := filepath.Join(targetDir, "natives")

	newGameDir := filepath.Join(newGamesDir, gameName)
	newJson := filepath.Join(newGameDir, "game.json")
	binDir := filepath.Join(newGameDir, "bin")
	libDir := filepath.Join(newGameDir, "lib")
	newControllerDir := filepath.Join(newGameDir, "controller")

	os.MkdirAll(binDir, 0777)

	jarName := getJarName(targetDir)
	jar := filepath.Join(targetDir, jarName)
	newJar := filepath.Join(binDir, gameName+".jar")

	os.Symlink(json, newJson)
	os.Symlink(jar, newJar)
	os.Symlink(nativesDir, libDir)
	os.Symlink(controllerDir, newControllerDir)

	log.Printf("[%v] has been compiled and symlinked to backend.\n", gameName)
}

func compileGame(gameName string) {
	cmd := "mvn clean compile assembly:single"
	parts := strings.Fields(cmd)

	log.Printf("[%v] is being compiled. This may take a while!\n", gameName)

	err := exec.Command(parts[0], parts[1:]...).Run()

	if err != nil {
		log.Println("Could not execute command:", cmd+". Reason:", err)
	}
}

func getJarName(path string) string {
	var jarName string

	d, err := os.Open(path)
	defer d.Close()

	if err != nil {
		log.Println(err)
		os.Exit(1)
	}

	files, err := d.Readdir(-1)

	if err != nil {
		log.Println(err)
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
