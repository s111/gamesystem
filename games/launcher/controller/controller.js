var backend;
var conn;

var game;
var games;
var sprite;
var target;
var selectedGame;

var y = 0;

var movePaddle = function(pos) {};

addMessageHandler(function(e) {
    var msg = JSON.parse(e.data);

    if (msg.action === "identify") {
        sendToBackend("identify", "launcher");
        sendToBackend("list");
    }

    if (msg.action === "list") {
        games = msg.data;

        game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {preload: preload, create: create});
    }

    movePaddle = function(pos) {
        if (selectedGame && pos === "start") {
            sendToGame("start", selectedGame);

            setTimeout(function() {
                document.location.href="/";
            }, 1000);
        } else {
            selectedGame = games[pos];

            sendToGame("select", games[pos]);
        }
    };
});

function preload() {
  game.stage.backgroundColor = '#000000';
}

function create() {
  game.scale.fullScreenScaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.scaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.refresh();

  for (var i = 0; i < games.length; i++) {
    var text = game.add.text(game.scale.width / 2, (game.scale.height - (game.scale.height / 16) * games.length) / 2 + (game.scale.height / 16) * i, games[i], {fill: "white"});
    text.x = (game.scale.width - text.width) / 2;
    console.log(games[i]);

    var sprite = game.add.sprite(0, 0);
    sprite.value = i;
    sprite.addChild(text);
    sprite.inputEnabled = true;
  }

  var text = game.add.text(game.scale.width / 2, (game.scale.height - (game.scale.height / 16) * games.length) / 2 + (game.scale.height / 16) * (i + 1), "START GAME", {fill: "red"});
  text.x = (game.scale.width - text.width) / 2;

  var sprite = game.add.sprite(0, 0);
  sprite.value = "start";
  sprite.addChild(text);
  sprite.inputEnabled = true;

  game.input.onDown.add(function(pointer) {
    if (!game.scale.isFullScreen) {
      game.scale.startFullScreen(false);
    }

    target = pointer.targetObject;

    if (target) {
      movePaddle(target.sprite.value);
    }
  }, this);
}
