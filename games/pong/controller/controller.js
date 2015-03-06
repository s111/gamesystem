var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {preload: preload, create: create, update: update});
var paddle;
var leftBttn;
var rightBttn;
var target;

var playingSide = "";

var leftAvailable = false;
var rightAvailable = false;

var selectionState;
var playingState;
var pendingStateChange = false;

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

      playingState = playingSide !== "";
      selectionState = !playingState;

      pendingStateChange = true;

      leftAvailable = left === "";
      rightAvailable = right === "";
    }
  });
}

function create() {
  game.scale.fullScreenScaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.scaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.refresh();

  game.stage.backgroundColor = '#000000';

  setupButtons();

  game.input.onDown.add(function(pointer) {
    if (!game.scale.isFullScreen) {
      game.scale.startFullScreen(false);
    } else if (pointer.targetObject) {
      selectPaddle(pointer.targetObject.sprite.data);
    }
    target = pointer.targetObject;
  }, this);
}

function setupButtons() {
  var bttnWidth = game.stage.width / 2;
  var bttnHeight = game.stage.height;

  for (i = 0; i < 2; i++) {
    var side = i < 1 ? "left" : "right";

    g = game.add.graphics(0, 0);

    var color = (side === "left" ? "0x22A7F0" : "0xF39C12");
    g.beginFill(color, 1);

    g.drawRect(0, 0, bttnWidth, bttnHeight);

    s = game.add.sprite(i * bttnWidth, 0);
    s.kill();
    s.addChild(g);
    s.data = side;
    s.inputEnabled = true;

    var text = game.add.text(bttnWidth / 2, bttnHeight / 2, side.toUpperCase(), {fill: "white"});
    text.x -= text.width / 2;
    s.addChild(text);
f
    if (side === "left") {
      leftBttn = s;
    } else {
      rightBttn = s;
    }
  }
}

function setupPaddle() {
  paddle = game.add.sprite(32, 32);
  paddle.inputEnabled = true;
  paddle.input.enableDrag();
  paddle.input.allowHorizontalDrag = false;
  paddle.input.boundsRect = new Phaser.Rectangle(32, 32, game.stage.width, game.stage.height - 32 - 128);
}

function setPaddleColor(side) {
  var color = (side === "left" ? "0x22A7F0" : "0xF39C12");

  var paddleGfx = game.add.graphics(0, 0);

  paddleGfx.beginFill(color, 1);
  paddleGfx.drawRect(0, 0, game.stage.width - 32*2, 128);

  paddle.addChild(paddleGfx);
}

function update() {
  if (target && target.sprite === paddle && target.isDragged) {
    movePaddle((paddle.y - 32)/(game.stage.height - 32*2 - 128));
  }

  if (pendingStateChange) {

    if (playingState) {
      if(!paddle) {
        setupPaddle();
      } else {
        paddle.revive();
      }

      setPaddleColor(playingSide);

      leftBttn.kill();
      rightBttn.kill();

      pendingStateChange = false;
    }

    else if (selectionState) {
      if (leftAvailable) {
        leftBttn.revive();
      } else {
        leftBttn.kill();
      }

      if (rightAvailable) {
        rightBttn.revive();
      } else {
        rightBttn.kill();
      }

      pendingStateChange = false;
    }
  }
}
