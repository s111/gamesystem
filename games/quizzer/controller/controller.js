var conn;

var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {preload: preload, create: create});
var target;
var bttnA;
var bttnB;
var bttnC;
var bttnD;
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

  var gfxA = game.add.graphics(0, 0);
  var gfxB = game.add.graphics(0, 0);
  var gfxC = game.add.graphics(0, 0);
  var gfxD = game.add.graphics(0, 0);

  gfxA.beginFill(0xFF0000, 0.5);
  gfxA.drawRect(0, 0, 128, 128);

  bttnA = game.add.sprite(100, 100);
  bttnA.addChild(gfxA);
  bttnA.inputEnabled = true;

  gfxB.beginFill(0xFFFF00, 0.5);
  gfxB.drawRect(0, 0, 128, 128);

  bttnB = game.add.sprite(250, 100);
  bttnB.addChild(gfxB);
  bttnB.inputEnabled = true;

  gfxC.beginFill(0xFF0000, 0.5);
  gfxC.drawRect(0, 0, 128, 128);

  bttnC = game.add.sprite(402, 100);
  bttnC.addChild(gfxC);
  bttnC.inputEnabled = true;

  gfxD.beginFill(0xFFFF00, 0.5);
  gfxD.drawRect(0, 0, 128, 128);

  bttnD = game.add.sprite(552, 100);
  bttnD.addChild(gfxD);
  bttnD.inputEnabled = true;

  game.input.onDown.add(function(pointer) {
    if (!game.scale.isFullScreen) {
      game.scale.startFullScreen(false);
    }

    target = pointer.targetObject;
  }, this);

  game.input.onTap.add(function(pointer) {
    target = pointer.targetObject;

    if (target && target.sprite == bttnA) {
      selection(1);
    }

    target = pointer.targetObject;
    if (target && target.sprite == bttnB) {
      selection(2);
    }

    target = pointer.targetObject;
    if (target && target.sprite == bttnC) {
      selection(3);
    }

    target = pointer.targetObject;
    if (target && target.sprite == bttnD) {
      selection(4);
    }
  }, this);
}
