var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {preload: preload, create: create, update: update});
var sprite;
var leftBttn;
var rightBttn;
var target;

var leftSelected = false;
var rightSelected = false;

var y = 0;

var movePaddle = function(pos) {};
var selectPaddle = function(sel){};

function preload() {
  addMessageHandler(function(msg) {
    if (msg === "identified") {
      movePaddle = function(pos) {
        sendToGame("move", pos);
      }
    }
    if (msg.action === "play as") {
      selectPaddle = function(sel) {
        sendToGame("play as", sel);
      }
    }
  });
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

  var btnGraphics1 = game.add.graphics(0, 0);
  btnGraphics1.beginFill(0xFF2245, 1);
  btnGraphics1.drawRect(0, game.stage.height/2 - 128, game.stage.width/2, 128);
  leftBttn = game.add.sprite(0, 0);
  leftBttn.addChild(btnGraphics1);
  leftBttn.inputEnabled = true;
  leftBttn.data = 0;
  var btnGraphics2 = game.add.graphics(0, 0);
  btnGraphics2.beginFill(0xFF0000, 1);
  btnGraphics2.drawRect(game.stage.width/2 + 10, game.stage.height/2 - 128, game.stage.width/2 - 10, 128);
  rightBttn = game.add.sprite(0, 0);
  rightBttn.addChild(btnGraphics2);
  rightBttn.inputEnabled = true;
  rightBttn.data = 1;

  game.input.onDown.add(function(pointer) {
    if (!game.scale.isFullScreen) {
      game.scale.startFullScreen(false);
    } else if (pointer.targetObject) {
     selectPaddle(pointer.targetObject.sprite.data);
     console.log(pointer.targetObject.sprite.data);
    }
    target = pointer.targetObject;
  }, this);
}

function update() {
  if (target && target.sprite === sprite && target.isDragged) {
    movePaddle((sprite.y - 32)/(game.stage.height - 32*2 - 128));
  }
}
