var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {preload: preload, create: create, update: update});
var paddle;
var leftBttn;
var rightBttn;
var target;

var playingSide = "";

var leftAvailable = false;
var rightAvailable = false;

var movePaddle = function(pos) {};

function selectPaddle(sel) {
  sendToGame("play as", sel);
}

function preload() {
  addMessageHandler(function(msg) {
    if (msg === "identified") {
      sendToGame("play as", "");

      movePaddle = function(pos) {
        sendToGame("move", pos);
      }
    }

    if (msg.action === "play as") {
      var id = getId();

      var left = msg.data.left;
      var right = msg.data.right;

      if (left === id) {
        playingSide = "left";
      } else if (right === id) {
        playingSide = "right";
      }

      leftAvailable = left === "";
      rightAvailable = right === "";
    }
  });
  game.stage.backgroundColor = '#000000';
}

function create() {
  game.scale.fullScreenScaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.scaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.refresh();

  var btnGraphics1 = game.add.graphics(0, 0);
  btnGraphics1.beginFill(0xFF2245, 1);
  btnGraphics1.drawRect(0, game.stage.height/2 - 128, game.stage.width/2, 128);
  leftBttn = game.add.sprite(0, 0);
  leftBttn.addChild(btnGraphics1);
  leftBttn.inputEnabled = true;
  leftBttn.data = "left";
  var btnGraphics2 = game.add.graphics(0, 0);
  btnGraphics2.beginFill(0xFF0000, 1);
  btnGraphics2.drawRect(game.stage.width/2 + 10, game.stage.height/2 - 128, game.stage.width/2 - 10, 128);
  rightBttn = game.add.sprite(0, 0);
  rightBttn.addChild(btnGraphics2);
  rightBttn.inputEnabled = true;
  rightBttn.data = "right";

  game.input.onDown.add(function(pointer) {
    if (!game.scale.isFullScreen) {
      game.scale.startFullScreen(false);
    } else if (pointer.targetObject) {
      selectPaddle(pointer.targetObject.sprite.data);
    }
    target = pointer.targetObject;
  }, this);
}

function update() {
  if (target && target.sprite === sprite && target.isDragged) {
    movePaddle((sprite.y - 32)/(game.stage.height - 32*2 - 128));
  }

  if (playing || !leftAvailable) {
    leftBttn.kill();
  } else {
    leftBttn.revive();
  }
}

function setupPaddle() {
  paddle = game.add.sprite(32, 32);
  paddle.inputEnabled = true;
  paddle.input.enableDrag();
  paddle.input.allowHorizontalDrag = false;
  paddle.input.boundsRect = new Phaser.Rectangle(32, 32, game.stage.width, game.stage.height - 32 - 128);
}

  if (playing || !rightAvailable) {
    rightBttn.kill();
  } else {
    rightBttn.revive();
function setPaddleColor(side) {
  var color = (side === "left" ? "0x22A7F0" : "0xF39C12");

  var paddleGfx = game.add.graphics(0, 0);

  paddleGfx.beginFill(color, 1);
  paddleGfx.drawRect(0, 0, game.stage.width - 32*2, 128);

  paddle.addChild(paddleGfx);
}

  }
}
