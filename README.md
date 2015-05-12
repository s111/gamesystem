# Game system
For this thesis we have developed a game system for couch multiplayer PC games to be played in social gatherings.
The main feature is to enable the use of smartphones as the game controllers.
It consists of the back end and a front end application enabling users to choose between games to play.
Sample games are included to ease the learning curve for developers seeking to create or adapt games for it.

The development was carried out using web technologies like HTML5 and JavaScript.
For all communication within the system the WebSocket protocol was used.
For developing the back end of the system the programming language Go was utilized.

The system is designed using the common client-server architecture, where the back end acts as the server and both the game and the smartphone controllers acts as clients.

## Table of Contents

- [Precompiled](#precompiled)
- [Develop](#develop)
- [Compile and package](#compile-and-package)
- [Screenshots](#screenshots)
	- [Launcher](#launcher)
	- [Controller](#controller)

## Precompiled
The game system packaged together with some sample game.  
[Linux](https://github.com/s111/gamesystem/releases/download/v1.0/gamesystem_linux.zip)  
[Windows](https://github.com/s111/gamesystem/releases/download/v1.0/gamesystem_windows.zip)  
[All Releases](https://github.com/s111/gamesystem/releases)

## Develop
Download the repository:
```sh
go get github.com/s111/gamesystem
```

Run the system:
```sh
cd $GOPATH/src/github.com/s111/gamesystem
go run main.go
```

For specifics on how to develop a game for the system, checkout the [manual pages](http://godoc.org/github.com/s111/gamesystem) and the sample games [Pong](https://github.com/s111/gs-pong), [TriggerHappy](https://github.com/s111/gs-triggerhappy) and [Quizzer](https://github.com/s111/gs-quizzer).

While developing a game for the system, you might want to run the game system in the background without the launcher:
```
go run main.go -scheduler=false
```
This is so you can run your own game from your IDE.

## Compile and package
```sh
go get github.com/s111/gamesystem
```
```sh
go install github.com/s111/gamesystem
```
There should now be a ```gamesystem``` binary in ```$GOPATH/bin```. In addition to the ```gamesystem``` binary, you need ```hub.js```, ```jquery.js``` and ```phaser.js``` to be in the same folder. You will also need to create a folder with the name ```games```, this is where the system looks for games. Now you need to compile and copy the [Launcher](https://github.com/s111/gs-launcher) into this folder.

## Screenshots

### Launcher
<img src="https://github.com/s111/gamesystem/blob/master/screenshots/launcher.png" width="640">

### Controller
<img src="https://github.com/s111/gamesystem/blob/master/screenshots/launcher_controller.png" width="320">

More [screenshots](https://github.com/s111/gamesystem/tree/master/screenshots)
