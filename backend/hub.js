var backend

function addMessageHandler(callback) {
  backend = new WebSocket('ws://' + window.location.hostname + ':3001/ws');

  backend.onmessage = function(e) {
    var msg = JSON.parse(e.data);

    if (msg.action === "identify") {
      var id = localStorage.getItem("id-gsusfcavf");

      if(id == null) {
        id = Math.random().toString(36).substring(7);

        localStorage.setItem("id-gsusfcavf", id)
      }

      sendToBackend("identify", id);

      callback("identified");
    } else {
      callback(msg);
    }
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
