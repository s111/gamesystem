var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {
    preload: preload,
    create: create
});

var pos;
var score;

var shootEnemy = function(pos) {}


function preload() {
    addMessageHandler(function(msg) {
        if (msg === "identified") {
            shootEnemy = function(pos) {
                sendToGame("shoot", pos);
            }
        }

        if (msg.action === "update score") {
            score.setText("SCORE: " + msg.data);
        }
    });

    game.stage.backgroundColor = '#2C3E59';
}

function create() {
    game.scale.fullScreenScaleMode = Phaser.ScaleManager.EXACT_FIT;
    game.scale.scaleMode = Phaser.ScaleManager.EXACT_FIT;
    game.scale.refresh();

    var bttnWidth = game.stage.width / 3;
    var bttnHeight = game.stage.height / 2;

    var sprite = game.add.sprite(game.stage.width / 2, game.stage.height / 2);

    var textStyle = {
        font: "48px Arial",
        fill: "#fff"
    };

    score = game.add.text(0, 0, "", textStyle);
    score.setText("SCORE: 0");
    score.x -= score.width / 2;
    score.y -= score.height / 2;
    sprite.addChild(score);

    for (i = 0; i < 6; i++) {
        g = game.add.graphics(0, 0);
        g.beginFill(0xE74C3C, 1);
        g.drawRect((i % 3) * bttnWidth + 32, (i > 2 ? 1 : 0) * bttnHeight + 32, bttnWidth - 64, bttnHeight - 64);

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