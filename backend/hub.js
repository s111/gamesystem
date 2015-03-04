var backend

$(function() {
  disconnect = createButton("Disconnect");
  disconnect.click(function () {
    sendToBackend("disconnect");
  });

  quit = createButton("Quit game");
  quit.click(function () {
    sendToBackend("start", "Launcher");
  });

  $("#game").before(disconnect);
  $("#game").before(quit);
});

function createButton(text) {
  button = $('<button></button>');
  button.html(text);
  button.css('width', '50%');
  button.css('height', '75px');
  button.css('font-weight', 'bold');

  return button;
}

function addMessageHandler(callback) {
  backend = new WebSocket('ws://' + window.location.hostname + ':3001/ws');

  backend.onmessage = function(e) {
    var msg = JSON.parse(e.data);

    if (msg.action === "identify") {
      if (!msg.data) {
        var id = localStorage.getItem("id-gsusfcavf");

        if(id == null) {
          id = Math.random().toString(36).substring(7);

          localStorage.setItem("id-gsusfcavf", id)
        }

        sendToBackend("identify", id);
      } else if (msg.data === "ok") {
        callback("identified");
      } else {
        var id = sessionStorage.getItem("id-gsusfcavf");

        if(id == null) {
          id = Math.random().toString(36).substring(7);

          sessionStorage.setItem("id-gsusfcavf", id)
        }

        sendToBackend("identify", id);
      }
    } else if (msg.action === "redirect") {
      var gameName = msg.data.toLowerCase()

      if (document.location.href.indexOf(gameName) < 0) {
        document.location.href = "/" + gameName;
      }
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

function getId() {
  var id;

  if (sessionStorage.getItem("id-gsusfcavf")) {
    id = sessionStorage.getItem("id-gsusfcavf");
  } else {
    id = localStorage.getItem("id-gsusfcavf");
  }

  return id;
}
