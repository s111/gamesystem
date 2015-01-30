var conn;

var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {preload: preload, create: create});
var sprite;
var target;

var y = 0;

var movePaddle = function(pos) {};

function preload() {
  conn = new WebSocket('ws://' + window.location.hostname + ':3001/ws');

  conn.onopen = function (e) {
    movePaddle = function(pos) {
      console.log(JSON.stringify({"action":"select", "data": pos}));
      conn.send(JSON.stringify({"action":"select", "data": pos}));

      document.location.href="/";
    };
  };

  conn.onmessage = function(e) {
    games = JSON.parse(e.data);

    for (var i = 0; i < games.length; i++) {
      var text = game.add.text(game.scale.width / 2, (game.scale.height - (game.scale.height / 16) * games.length) / 2 + (game.scale.height / 16) * i, games[i].Name, {fill: "white"});
      text.x = (game.scale.width - text.width) / 2;
      console.log(games[i].Name);

      var sprite = game.add.sprite(0, 0);
      sprite.value = i + 1;
      sprite.addChild(text);
      sprite.inputEnabled = true;
    }
  };

  game.stage.backgroundColor = '#000000';
}

function create() {
  game.scale.fullScreenScaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.scaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.refresh();

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
