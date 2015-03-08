var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {preload: preload, create: create, update: update});
var paddle;
var leftBttn;
var rightBttn;
var target;

var gameIsFullMsg;

var playingSide = "";

var leftAvailable = false;
var rightAvailable = false;
var onlyOneAvailable;

var selectionState;
var playingState;
var pendingStateChange = false;

var textStyle = {font: "48px Arial", fill: "#fff"};

var numberOfButtonsLeftToRender;

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

      onlyOneAvailable = (leftAvailable && !rightAvailable) || (!leftAvailable && rightAvailable);

      numberOfButtonsLeftToRender = (onlyOneAvailable ? 1 : 2);
    }
  });
}

function create() {
  game.scale.fullScreenScaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.scaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.refresh();

  game.stage.backgroundColor = '#000000';

  setupGameIsFullMsg();

  game.input.onDown.add(function(pointer) {
    if (!game.scale.isFullScreen) {
      game.scale.startFullScreen(false);
    } else if (pointer.targetObject) {
      selectPaddle(pointer.targetObject.sprite.data);
    }
    target = pointer.targetObject;
  }, this);
}

function setupButton(leftOrRight) {
  var bttnX = game.stage.width / 2;
  var bttnWidth = game.stage.width / 2;
  var bttnHeight = game.stage.height;

  if (numberOfButtonsLeftToRender === 2) {
    bttnX = 0;
  }

  if (onlyOneAvailable) {
    bttnX = 0;
    bttnWidth *= 2;
  }

  g = game.add.graphics(0, 0);

  var color = (leftOrRight === "left" ? "0x22A7F0" : "0xF39C12");
  g.beginFill(color, 1);

  g.drawRect(0, 0, bttnWidth, bttnHeight);

  s = game.add.sprite(bttnX, 0);

  s.addChild(g);
  s.data = leftOrRight;
  s.inputEnabled = true;

  var text = game.add.text(bttnWidth / 2, bttnHeight / 2, "", textStyle);
  text.setText("PLAY AS " + leftOrRight.toUpperCase());
  text.x -= text.width / 2;
  s.addChild(text);

  numberOfButtonsLeftToRender--;

  return s;
}

function setupGameIsFullMsg() {
  gameIsFullMsg = game.add.sprite(0, 0);
  gameIsFullMsg.kill();
  var text = game.add.text(game.stage.width / 2, game.stage.height / 2, "THE GAME IS FULL!", textStyle);
  text.x -= text.width / 2;
  gameIsFullMsg.addChild(text);
}

function setupPaddle(playingSide) {
  var color = (playingSide === "left" ? "0x22A7F0" : "0xF39C12");

  var paddleGfx = game.add.graphics(0, 0);
  paddleGfx.beginFill(color, 1);
  paddleGfx.drawRect(0, 0, game.stage.width - 32*2, 128);

  paddle = game.add.sprite(32, 32);
  paddle.addChild(paddleGfx);

  paddle.inputEnabled = true;
  paddle.input.enableDrag();
  paddle.input.allowHorizontalDrag = false;
  paddle.input.boundsRect = new Phaser.Rectangle(32, 32, game.stage.width, game.stage.height - 32 - 128);
}

function update() {
  if (target && target.sprite === paddle && target.isDragged) {
    movePaddle((paddle.y - 32)/(game.stage.height - 32*2 - 128));
  }

  if (pendingStateChange) {

    destroy(leftBttn);
    destroy(rightBttn);

    pendingStateChange = false;

    if (playingState) {

      if (!paddle) {
        setupPaddle(playingSide);
      }
    }

    else if (selectionState) {
      var twoAvailable = leftAvailable && rightAvailable;

      if (twoAvailable) {
        leftBttn = setupButton("left");
        rightBttn = setupButton("right");
      } else if (onlyOneAvailable) {
        if (leftAvailable) {
          leftBttn = setupButton("left");

          gameIsFullMsg.kill();
        } else if (rightAvailable) {
          rightBttn = setupButton("right");

          gameIsFullMsg.kill();
        }
      } else {
        gameIsFullMsg.revive();
      }
    }
  }
}

function destroy(sprite) {
  if (sprite) {
    sprite.destroy();
  }
}
