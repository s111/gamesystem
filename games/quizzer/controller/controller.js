var conn;

var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {preload: preload, create: create});
var data;

var bttnHeight;
var bttnWidth;
var colours = [0xFF0000, 0xFFFF00, 0x00FF00, 0x0000FF];
var selection = function(sel) {};

function preload() {
  conn = new WebSocket('ws://' + window.location.hostname + ':1234/ws');

  conn.onopen = function(e) {
    selection = function(sel) {
      conn.send(sel)
    }
  }

  game.stage.backgroundColor = '#000000';
}

function create() {
  game.scale.fullScreenScaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.scaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.refresh();

  bttnWidth = game.stage.width / 2;
  bttnHeight = game.stage.height / 2;

  for (i = 1; i <= 4; i++) {
    g = game.add.graphics(0, 0);
    g.beginFill(colours[i - 1], 1);
    g.drawRect(((i == 2 || i == 4) ? 1 : 0) * bttnWidth + 32, (i > 2 ? 1 : 0) * bttnHeight + 32, bttnWidth - 64, bttnHeight - 64);
    s = game.add.sprite(0, 0);
    s.addChild(g);
    s.data = i;
    s.inputEnabled = true;
  }

  game.input.onDown.add(function(pointer) {
    if (!game.scale.isFullScreen) {
      game.scale.startFullScreen(false);
    } else if (pointer.targetObject) {
        selection(pointer.targetObject.sprite.data);
    }
  }, this);
}
