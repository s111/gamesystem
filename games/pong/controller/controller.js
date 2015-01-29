var conn;

var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {preload: preload, create: create, update: update});
var sprite;
var target;

var y = 0;

var movePaddle = function(pos) {};

function preload() {
  conn = new WebSocket('ws://' + window.location.hostname + ':1234/ws');

  conn.onopen = function (e) {
    movePaddle = function(pos) {
      conn.send(pos);
    }
  }

  game.stage.backgroundColor = '#000000';
}

function create() {
  game.scale.fullScreenScaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.scaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.refresh();

  var graphics = game.add.graphics(0, 0);

  graphics.beginFill(0xFFFFFF, 1);
  graphics.drawRect(0, 0, game.stage.width - 32*2, 128);

  sprite = game.add.sprite(32, 32);
  sprite.addChild(graphics);
  sprite.inputEnabled = true;
  sprite.input.enableDrag();
  sprite.input.allowHorizontalDrag = false;
  sprite.input.boundsRect = new Phaser.Rectangle(32, 32, game.stage.width, game.stage.height - 32 - 128);

  game.input.onDown.add(function(pointer) {
    if (!game.scale.isFullScreen) {
      game.scale.startFullScreen(false);
    }

    target = pointer.targetObject;
  }, this);
}

function update() {
  if (target && target.sprite === sprite && target.isDragged) {
    movePaddle((sprite.y - 32)/(game.stage.height - 32*2 - 128));
  }
}
