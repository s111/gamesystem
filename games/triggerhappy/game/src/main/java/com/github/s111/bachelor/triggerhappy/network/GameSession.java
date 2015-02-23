package com.github.s111.bachelor.triggerhappy.network;

import com.github.s111.bachelor.triggerhappy.game.Triggerhappy;
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
import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private final Triggerhappy game;

    private List<Player> players;

    private Session backend;

    public GameSession(Triggerhappy game) throws DeploymentException {
        this.game = game;
        players = new ArrayList();
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

                players.add(new Player(id));

                break;
            }
            case "dropped client": {
                String id = jsonObj.getString("data");

                for (Player player : players) {
                    if (player.getId().equals(id)) {
                        players.remove(player);
                    }
                }
                break;
            }
            case "shoot": {
                String data = jsonObj.getJsonNumber("data").toString();

                int position;

                try {
                    position = Integer.parseInt(data);
                } catch (Exception e) {
                    position = -1;
                }

                if (!jsonObj.containsKey("from")) {
                    return;
                }

                String from = jsonObj.getString("from");

                for (Player player : players) {
                    if (player.getId().equals(from)) {
                        if (game.checkIfHit(position)) {
                            player.increaseScore();
                        }
                    }
                }
            }
        }
    }

    public List<Integer> getScores() {
        List<Integer> scores = new ArrayList<Integer>();
        for (Player player : players) {
            scores.add(player.getScore());
        }
        return scores;
    }

    private class Player {
        private String id;
        private int score = 0;

        private Player(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void increaseScore() {
            score++;
        }

        public int getScore() {
            return score;
        }
    }
}
