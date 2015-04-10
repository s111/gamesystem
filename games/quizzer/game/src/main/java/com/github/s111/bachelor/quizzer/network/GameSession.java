package com.github.s111.bachelor.quizzer.network;

import com.github.s111.bachelor.quizzer.game.Quizzer;
import org.glassfish.tyrus.client.ClientManager;

import javax.json.*;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class GameSession {
    private final Quizzer game;

    private Set<Player> players;
    private Set<Player> correctPlayers;
    private List<Player> sortedPlayers;

    private Session backend;

    public GameSession(Quizzer game) throws DeploymentException {
        this.game = game;
        players = new HashSet<>();
        correctPlayers = new HashSet<>();
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

    public void onMessage(Session session, String message) throws IOException, EncodeException {
        JsonReader jsonReader = Json.createReader(new StringReader(message));
        JsonObject jsonObj = jsonReader.readObject();
        jsonReader.close();

        if (!(jsonObj.containsKey("action") && jsonObj.containsKey("data"))) {
            return;
        }

        String action = jsonObj.getString("action");

        switch (action) {
            case "identify": {
                String data = jsonObj.getString("data");

                if (data.equals("ok")) {
                    backend.getBasicRemote().sendObject(Json.createObjectBuilder()
                            .add("action", "get clients")
                            .build());
                }
                break;
            }
            case "get clients": {
                if (jsonObj.isNull("data")) {
                    break;
                }
                JsonArray clients = jsonObj.getJsonArray("data");

                for (JsonValue client : clients) {
                    players.add(new Player(client.toString()));
                }

                break;
            }
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
                        break;
                    }
                }
                break;
            }
            case "get username": {
                JsonArray client = jsonObj.getJsonArray("data");

                String id = client.get(0).toString();
                String username = client.get(1).toString();

                for (Player player : players) {
                    if (player.getId().equals(id)) {
                        player.setUserName(username);

                        break;
                    }
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

                if (!jsonObj.containsKey("from")) {
                    return;
                }

                String from = jsonObj.getString("from");

                for (Player player : players) {
                    if (player.getId().equals(from) && !player.hasAnswered()) {
                        player.setAnswered(true);
                        if (game.checkIfCorrectAnswer(selection)) {
                            if (correctPlayers.size() < 4) {
                                correctPlayers.add(player);
                            }
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

    public void updateScores() {
        int multiplier = 0;
        for (Player player : correctPlayers) {
            player.increaseScore(10 - 2 * multiplier);
            multiplier++;
        }
        correctPlayers.clear();

        for (Player player : players) {
            player.setAnswered(false);
        }
    }

    public List<Player> getTopThree() {
        sortedPlayers = new ArrayList<>(players);
        Collections.sort(sortedPlayers, new PlayerScoreComparator());
        if (sortedPlayers.size() > 3) {
            sortedPlayers.subList(3, sortedPlayers.size() - 1);
            return sortedPlayers;
        } else if (sortedPlayers.size() == 2) {
            sortedPlayers.add(new Player("No 3rd!"));
        } else if (sortedPlayers.size() == 1) {
            sortedPlayers.add(new Player("No 2nd!"));
            sortedPlayers.add(new Player("No 3rd!"));
            return sortedPlayers;
        } else if (sortedPlayers.isEmpty()) {
            sortedPlayers.add(new Player("No 1st!"));
            sortedPlayers.add(new Player("No 2nd!"));
            sortedPlayers.add(new Player("No 3rd!"));
        } else {
            return sortedPlayers;
        }
        return sortedPlayers;
    }

    public class Player {
        private String id;
        private String userName;
        private boolean hasAnswered;
        private int score = 0;

        private Player(String id) {
            this.id = id;
            this.userName = id;
            this.hasAnswered = false;
        }

        public String getId() {
            return id;
        }

        public void increaseScore(int number) {
                score += number;
        }

        public boolean hasAnswered() {
            return hasAnswered;
        }

        public int getScore() {
            return score;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String name) {
            this.userName = name;
        }

        @Override
        public boolean equals(Object o) {
            return ((Player) o).getId().equals(getId());
        }

        @Override
        public int hashCode() {
            return getId().hashCode();
        }

        public void setAnswered(boolean bool) {
            hasAnswered = bool;
        }
    }

    class PlayerScoreComparator implements Comparator<Player> {
        public int compare(Player player1, Player player2) {
            return player1.getScore() - player2.getScore();
        }
    }
}
