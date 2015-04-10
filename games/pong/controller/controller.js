var game = new Phaser.Game(800, 600, Phaser.AUTO, 'game', {
    preload: preload,
    create: create,
    update: update
});

var paddle;
var leftButton;
var rightButton;
var target;

var playingSide = "";

var leftPaddleAvailable = false;
var rightPaddleAvailable = false;
var onlyOnePaddleAvailable;

var selectionState;
var playingState;
var pendingStateChange = false;

var oldPos;

var RED = "0xff0373";
var GREEN = "0x00fc8c";
var BLACK = "0x111213";

var textStyle = {
    font: "38px Arial",
    fill: "#eeefef",
    strokeThickness: 5,
    stroke: "#111213"
};

var numberOfButtonsLeftToRender;

var gameIsFullMessage;

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

            leftPaddleAvailable = left === "";
            rightPaddleAvailable = right === "";

            onlyOnePaddleAvailable = (leftPaddleAvailable && !rightPaddleAvailable) || (!leftPaddleAvailable && rightPaddleAvailable);

            numberOfButtonsLeftToRender = (onlyOnePaddleAvailable ? 1 : 2);
        }
    });
}

function create() {
    game.scale.fullScreenScaleMode = Phaser.ScaleManager.EXACT_FIT;
    game.scale.scaleMode = Phaser.ScaleManager.EXACT_FIT;
    game.scale.refresh();

    game.stage.backgroundColor = BLACK;

    game.input.onDown.add(function(pointer) {
        var data;

        if (pointer.targetObject) {
            data = pointer.targetObject.sprite.data;
        }

        if (!game.scale.isFullScreen) {
            game.scale.startFullScreen(false);
        } else if (data === "left" || data === "right") {
            selectPaddle(data);
        }

        target = pointer.targetObject;
    }, this);
}

function createPaddleSprite(playingSide) {
    var color = (playingSide === "left" ? RED : GREEN);

    var g = game.add.graphics(0, 0);
    g.beginFill(color, 1);
    g.drawRect(0, 0, game.stage.width, 128);

    s = game.add.sprite(0, 32);
    s.addChild(g);

    s.inputEnabled = true;
    s.input.enableDrag();
    s.input.allowHorizontalDrag = false;
    s.input.boundsRect = new Phaser.Rectangle(0, 32, game.stage.width, game.stage.height - 32 - 128);

    paddle = s;
}

function createButtonSprites(leftOrRight) {
    var buttonX = game.stage.width / 2;
    var buttonWidth = game.stage.width / 2;
    var buttonHeight = game.stage.height;

    if (numberOfButtonsLeftToRender === 2) {
        buttonX = 0;
    }

    if (onlyOnePaddleAvailable) {
        buttonX = 0;
        buttonWidth *= 2;
    }

    var x = 32;
    if (leftOrRight === "right") {
        x = 0;
    }

    g = game.add.graphics(0, 0);

    var color = (leftOrRight === "left" ? RED : GREEN);
    g.beginFill(color, 1);

    g.drawRoundedRect(x, 32, buttonWidth - 32 - x, buttonHeight - 64, 64);

    s = game.add.sprite(buttonX, 0);

    s.addChild(g);
    s.data = leftOrRight;
    s.inputEnabled = true;

    var text = game.add.text((buttonWidth - ((buttonX > 0) ? 32 : 0)) / 2, buttonHeight / 2, "", textStyle);
    text.setText("PLAY AS " + leftOrRight.toUpperCase());
    text.x -= text.width / 2;
    text.y -= text.height / 2;
    s.addChild(text);

    numberOfButtonsLeftToRender--;

    return s;
}

function createGameIsFullSprite() {
    var s = game.add.sprite(0, 0);
    s.kill();
    var text = game.add.text(game.stage.width / 2, game.stage.height / 2, "THE GAME IS FULL!", textStyle);
    text.x -= text.width / 2;
    s.addChild(text);

    gameIsFullMessage = s;
}

function update() {
    if (target && target.sprite === paddle && target.isDragged) {
        var newPos = ((paddle.y - 32) / (game.stage.height - 32 * 2 - 128) * 2000) | 0;

        if (newPos != oldPos) {
            oldPos = newPos;
            movePaddle(newPos / 2000);
        }
    }

    if (pendingStateChange) {
        pendingStateChange = false;

        destroy(leftButton);
        destroy(rightButton);

        if (playingState) {
            if (!paddle) {
                createPaddleSprite(playingSide);
            }
        } else if (selectionState) {
            if (!gameIsFullMessage) {
                createGameIsFullSprite();
            }

            var twoPaddlesAvailable = leftPaddleAvailable && rightPaddleAvailable;

            if (twoPaddlesAvailable) {
                leftButton = createButtonSprites("left");
                rightButton = createButtonSprites("right");
            } else if (onlyOnePaddleAvailable) {
                if (leftPaddleAvailable) {
                    leftButton = createButtonSprites("left");

                    gameIsFullMessage.kill();
                } else if (rightPaddleAvailable) {
                    rightButton = createButtonSprites("right");

                    gameIsFullMessage.kill();
                }
            } else {
                gameIsFullMessage.revive();
            }
        }
    }
}

function destroy(sprite) {
    if (sprite) {
        sprite.destroy();
    }
}