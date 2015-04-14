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

          if (index === 0) {
            $("#lst" + game).addClass("selected");

            $("#title").html(game);
            $("#screenshot-src").attr("src", "http://localhost:3001/img/" + game.toLowerCase() + ".png");
          }
        });

        break;
        case "select":
          $("#games li").each(function(index) {
            $(this).removeClass("selected");
          });

          $("#lst" + msg.data).addClass("selected");

          $("#title").html(msg.data);
          $("#screenshot-src").attr("src", "http://localhost:3001/img/" + msg.data.toLowerCase() + ".png");

          sendToBackend("get players", msg.data);
          sendToBackend("get description", msg.data);

          break;
          case "start":
            sendToBackend("start", msg.data);
            gui.App.quit();

            break;
            case "get description":
              $("#desc-text").html(msg.data);

              break;
              case "get players":
                if (msg.data === 0) {
                  $("#players").html(" Unlimited");
                } else {
                  $("#players").html(" " + msg.data);
                }

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

            backend.onclose = function(e) {
              gui.App.quit();
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
