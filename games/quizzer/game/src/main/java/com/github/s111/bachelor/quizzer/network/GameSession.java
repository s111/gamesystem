package com.github.s111.bachelor.quizzer.network;

import com.github.s111.bachelor.quizzer.game.Quizzer;
import org.glassfish.tyrus.client.ClientManager;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

public class GameSession {
    private final Quizzer game;

    private Player player;

    private Session backend;

    public GameSession(Quizzer game) throws DeploymentException {
        this.game = game;
    }

    public void connect() throws URISyntaxException, IOException, DeploymentException {
        ClientManager client = ClientManager.createClient();
        client.connectToServer(WebsocketClient.class, new URI("ws://localhost:3001/ws"));
    }

    public void onOpen(Session session) throws IOException, EncodeException {
        backend = session;
        backend.getBasicRemote().sendObject(Json.createObjectBuilder()
                .add("action", "identify")
                .add("data", "game")
                .build());
    }

    public void onMessage(Session session, String message) throws IOException {
        JsonReader jsonReader = Json.createReader(new StringReader(message));
        JsonObject jsonObj = jsonReader.readObject();
        jsonReader.close();

        String action = jsonObj.getString("action");

        switch (action) {
            case "added client": {
                String id = jsonObj.getString("data");

                player = new Player(id);

                break;
            }
            case "dropped client": {
                String id = jsonObj.getString("data");

                if (player.getId() == id) {
                    player.setId("");
                }
                break;
            }
            case "answer": {
                String data = jsonObj.getJsonNumber("data").toString();

                int selection;
                try {
                    selection = Integer.parseInt(data);
                } catch (NumberFormatException e) {
                    selection = -1;
                }

                game.checkIfCorrectAnswer(selection);
            }
        }
    }

    private class Player {
        private String id;

        public Player(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
