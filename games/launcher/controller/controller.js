var backend;
var conn;

var game;
var games;
var sprite;
var target;

var y = 0;

var movePaddle = function(pos) {};

backend = new WebSocket('ws://' + window.location.hostname + ':3001/ws');

backend.onmessage = function(e) {
  var data = JSON.parse(e.data);

  if (data === "ready") {
    conn = new WebSocket('ws://' + window.location.hostname + ':1234/ws');

    conn.onopen = function (e) {
      movePaddle = function(pos) {
        if (pos === "start") {
          conn.send(JSON.stringify({"action":"select", "data": pos}));

          document.location.href="/";
        } else {
          conn.send(JSON.stringify({"action":"select", "data": games[pos].Name}));
        }
      };
    };

    return;
  }

  games = data;

  game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {preload: preload, create: create});
};

function preload() {
  game.stage.backgroundColor = '#000000';
}

function create() {
  game.scale.fullScreenScaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.scaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.refresh();

  for (var i = 0; i < games.length; i++) {
    var text = game.add.text(game.scale.width / 2, (game.scale.height - (game.scale.height / 16) * games.length) / 2 + (game.scale.height / 16) * i, games[i].Name, {fill: "white"});
    text.x = (game.scale.width - text.width) / 2;
    console.log(games[i].Name);

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
