var backend;
var conn;

var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {preload: preload, create: create});

var pos;

var bttnHeight;
var bttnWidth;

var shootEnemy = function(pos) { }


function preload() {
  backend = new WebSocket('ws://localhost:3001/ws');

  backend.onmessage = function(e) {
    if (JSON.parse(e.data) === "ready") {
      conn = new WebSocket('ws://' + window.location.hostname + ':1234/ws');

      conn.onopen = function (e) {
        shootEnemy = function(pos) {
          window.navigator.vibrate(200);
          conn.send(pos)
        }
      }
    }
  };

  game.stage.backgroundColor = '#2C3E59';
}

function create() {
  game.scale.fullScreenScaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.scaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.refresh();

  bttnWidth = game.stage.width / 3;
  bttnHeight = game.stage.height / 2;

  for (i=0; i < 6; i++) {
    g = game.add.graphics(0, 0);
    g.beginFill(0xE74C3C, 1);
    g.drawRect((i%3) * bttnWidth + 32, (i > 2 ? 1 : 0) * bttnHeight + 32, bttnWidth - 64, bttnHeight - 64);

    s = game.add.sprite(0, 0);
    s.addChild(g);
    s.data = i;
    s.inputEnabled = true;
  }

  game.input.onDown.add(function(pointer) {
    if (!game.scale.isFullScreen) {
      game.scale.startFullScreen(false);
    } else {
      if (pointer.targetObject) {
        shootEnemy(pointer.targetObject.sprite.data + 1);
      }
    }
  }, this);
}
