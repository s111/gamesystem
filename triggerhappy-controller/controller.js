var conn;

var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {preload: preload, create: create, update: update});

var topLeft;
var topMiddle;
var topRight;
var botLeft;
var botMiddle;
var botRight;

var bttnHeight;
var bttnWidth;

var shootEnemy = function(pos) { console.log(pos) }


function preload() {
  conn = new WebSocket('ws://192.168.1.221:1234/ws');

  conn.onopen = function (e) {
    shootEnemy = function(pos) {
      conn.send(pos)
    }
  }

  game.stage.backgroundColor = '#000000';
}

function create() {
  game.scale.fullScreenScaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.scaleMode = Phaser.ScaleManager.EXACT_FIT;
  game.scale.refresh();

  bttnWidth = game.stage.width / 3;
  bttnHeight = game.stage.height / 2;

  // Draw graphics for buttons

  var topLeftGfx = game.add.graphics(0, 0);
  topLeftGfx.beginFill(0xD24D57, 1);
  topLeftGfx.drawRect(0, 0, bttnWidth, bttnHeight); // Top Left
 
  var topMiddleGfx = game.add.graphics(bttnWidth, 0); 
  topMiddleGfx.beginFill(0xDB0A5B, 1);
  topMiddleGfx.drawRect(0, 0, bttnWidth, bttnHeight); // Top Middle

  var topRightGfx = game.add.graphics(bttnWidth * 2, 0);
  topRightGfx.beginFill(0xF64747, 1);
  topRightGfx.drawRect(0, 0, bttnWidth, bttnHeight); // Top Right
  
  var botLeftGfx = game.add.graphics(0, bttnHeight);
  botLeftGfx.beginFill(0xF1A9A0, 1);
  botLeftGfx.drawRect(0, 0, bttnWidth, bttnHeight); // Bottom Left

  var botMiddleGfx = game.add.graphics(bttnWidth, bttnHeight);
  botMiddleGfx.beginFill(0xD2527F, 1);
  botMiddleGfx.drawRect(0, 0, bttnWidth, bttnHeight); // Bottom Middle

  var botRightGfx = game.add.graphics(bttnWidth * 2, bttnHeight);
  botRightGfx.beginFill(0xF62459, 1);
  botRightGfx.drawRect(0, 0, bttnWidth, bttnHeight); // Bottom Right

  // Add sprites for buttons and listen for clicks

  topLeft = game.add.sprite(0, 0);
  topLeft.addChild(topLeftGfx);
  topLeft.inputEnabled = true;
  topLeft.events.onInputDown.add(onDown, this);

  topMiddle = game.add.sprite(0, 0);
  topMiddle.addChild(topMiddleGfx);
  topMiddle.inputEnabled = true;
  topMiddle.events.onInputDown.add(onDown, this);

  topRight = game.add.sprite(0, 0);
  topRight.addChild(topRightGfx);
  topRight.inputEnabled = true;
  topRight.events.onInputDown.add(onDown, this);

  botLeft = game.add.sprite(0, 0);
  botLeft.addChild(botLeftGfx);
  botLeft.inputEnabled = true;
  botLeft.events.onInputDown.add(onDown, this);  

  botMiddle = game.add.sprite(0, 0);
  botMiddle.addChild(botMiddleGfx);
  botMiddle.inputEnabled = true;
  botMiddle.events.onInputDown.add(onDown, this);  

  botRight = game.add.sprite(0, 0);
  botRight.addChild(botRightGfx);
  botRight.inputEnabled = true;
  botRight.events.onInputDown.add(onDown, this);  

  game.input.onDown.add(function(pointer) {
    if (!game.scale.isFullScreen) {
      game.scale.startFullScreen(false);
    }

  }, this);
}

function onDown(sprite, pointer) {
  if(pointer.y <= bttnHeight) {
      if(pointer.x <= bttnWidth) {
        shootEnemy(1);
      } else if (pointer.x > bttnWidth && pointer.x <= bttnWidth * 2) {
        shootEnemy(2); 
      } else if (pointer.x >= bttnWidth * 2) {
        shootEnemy(3);
      } 
  } else {
    if(pointer.x <= bttnWidth) {
        shootEnemy(4);
    } else if (pointer.x > bttnWidth && pointer.x <= bttnWidth * 2) {
        shootEnemy(5);
    } else if (pointer.x >= bttnWidth * 2) {
        shootEnemy(6);
    }
  }
}

function update() {
}
