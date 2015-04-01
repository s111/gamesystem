var gui = require('nw.gui');
var backend

$(function() {
    addMessageHandler(function(msg) {
        if (msg === "identified") {
            sendToBackend("list");
        }

        switch (msg.action) {
            case "list":
                $.each(msg.data, function(index, game) {
                    $("#games").append("<li id=\"lst" + game + "\">" + game + "</li>");
                });
                break;
            case "select":
                $("#games li").each(function(index) {
                    $(this).css({
                        "color": "black",
                        "font-weight": "normal"
                    });
                });

                $("#lst" + msg.data).css({
                    "color": "blue",
                    "font-weight": "bold"
                });

                break;
            case "start":
                sendToBackend("start", msg.data);
                gui.App.quit();
                break;
        }
    });
});

function addMessageHandler(callback) {
    backend = new WebSocket('ws://localhost:3001/ws');
    backend.onmessage = function(e) {
        var msg = JSON.parse(e.data);
        if (msg.action === "identify") {
            if (!msg.data) {
                sendToBackend("identify", "game");
            } else if (msg.data === "ok") {
                callback("identified");
            }
        } else {
            callback(msg);
        }
    }
}

function sendToBackend(action, data) {
    send({
        "action": action,
        "data": data
    });
}

function send(json) {
    if (backend) {
        backend.send(JSON.stringify(json));
    }
}