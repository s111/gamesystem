var backend

function addMessageHandler(callback) {
    backend = new WebSocket('ws://' + window.location.hostname + ':3001/ws');
    backend.onmessage = function(e) {
        callback(e);
    }   
}

function sendToGame(action, data) {
        send({"to": "game", "action": "passthrough", "data": {"action": action, "data": data}});
}

function sendToBackend(action, data) {
    send({"action": action, "data": data});
}

function send(json) {
    if (backend) {
        backend.send(JSON.stringify(json));
    }
}
