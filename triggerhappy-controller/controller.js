var conn;

var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {preload: preload, create: create});

var pos;

var bttnHeight;
var bttnWidth;

var shootEnemy = function(pos) { console.log(pos) }


function preload() {
  conn = new WebSocket('ws://192.168.1.26:1234/ws');

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

  var colors = [0xD24D57, 0xDB0A5B, 0xF64747, 0xF1A9A0, 0xD2527F, 0xF62459];

  for (i=0; i < 6; i++) {
    g = game.add.graphics(0, 0);
    g.beginFill(colors[i], 1);
    g.drawRect((i%3) * bttnWidth, (i > 2 ? 1 : 0) * bttnHeight, bttnWidth, bttnHeight);

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
