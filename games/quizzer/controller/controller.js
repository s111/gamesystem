var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {
    preload: preload,
    create: create
});
var data;

var optionSprite = [];

var bttnWidth;
var bttnHeight;
var colours = [0xFF0373, 0xFCFF02, 0x00FC8C, 0x009CFF];

var selection = function(sel) {};

function preload() {
    addMessageHandler(function(msg) {
        if (msg === "identified") {
            selection = function(sel) {
                sendToGame("answer", sel);
                createSelectedOptionSprite(sel);
            }
        }
        if (msg.action === "next") {
            selectOptionSprite.kill();

            for (i = 0; i <= 3; i++) {
                optionSprite[i].revive();
            }
        }
    });

    game.stage.backgroundColor = '#111213';
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
        optionSprite[i - 1] = game.add.sprite(0, 0);
        optionSprite[i - 1].addChild(g);
        optionSprite[i - 1].data = i;
        optionSprite[i - 1].inputEnabled = true;
    }

    game.input.onDown.add(function(pointer) {
        if (!game.scale.isFullScreen) {
            game.scale.startFullScreen(false);
        } else if (pointer.targetObject) {
            selection(pointer.targetObject.sprite.data);
        }
    }, this);
}

function createSelectedOptionSprite(selection) {
    g = game.add.graphics(0, 0);
    g.beginFill(colours[selection - 1], 1);
    g.drawRect(0, 0, game.stage.width, game.stage.height);
    selectOptionSprite = game.add.sprite(0, 0);
    selectOptionSprite.addChild(g);

    for (i = 0; i <= 3; i++) {
        optionSprite[i].kill();
    }
}