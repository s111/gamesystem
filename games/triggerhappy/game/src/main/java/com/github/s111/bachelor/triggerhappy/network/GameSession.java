package com.github.s111.bachelor.triggerhappy.network;

import com.github.s111.bachelor.triggerhappy.game.Triggerhappy;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.server.Server;

import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private final Triggerhappy game;

    private List<Player> players;

    public GameSession(Triggerhappy game) throws DeploymentException {
        this.game = game;
        players = new ArrayList();
        Server server = new Server("localhost", 1234, "/", null, WebsocketServer.class);
        server.start();

        sendReady();
    }
    public void onOpen(Session session) throws IOException {
        players.add(new Player(session));

        RemoteEndpoint.Basic remote = session.getBasicRemote();
        remote.sendPing(ByteBuffer.wrap("".getBytes()));
    }

    public void onMessage(Session session, String message) throws IOException {
        int position;
        try {
            position = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            position = -1;
        }
        for (Player player : players) {
            if (session.equals(player.getSession())) {
                if(game.checkIfHit(position)) {
                    player.increaseScore();
                }
            }
        }
    }

    private void sendReady() {
        ClientManager client = ClientManager.createClient();

        try {
            client.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    JsonObject b = Json.createObjectBuilder()
                            .add("action", "ready")
                            .build();

                    session.getAsyncRemote().sendObject(b);
                }
            }, new URI("ws://localhost:3001/ws"));
        } catch (Exception e) {
            System.out.println("Unable to recover; exiting...");
            System.exit(1);
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
        private Session session;
        private int score = 0;

        private Player(Session session) {
            this.session = session;
        }

        public Session getSession() {
            return session;
        }

        public void increaseScore() {
            score++;
        }

        public int getScore() {
            return score;
        }
    }
}
