package com.github.s111.bachelor.triggerhappy.network;

import com.github.s111.bachelor.triggerhappy.game.Triggerhappy;

import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
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

    public List<Integer> getScores() {
        List<Integer> scores = new ArrayList<Integer>();
        for (Player player : players) {
            scores.add(player.getScore());
        }
        return scores;
    }
//    public void broadcastScore(int player1score, int player2score) {
//        String score = "[" + player1score + ", " + player2score + "]";
//        try {
//            if (player1 != null && player1.isOpen()) {
//                RemoteEndpoint.Basic remote1 = player1.getBasicRemote();
//                remote1.sendText(score);
//            }
//            if (player2 != null && player2.isOpen()) {
//                RemoteEndpoint.Basic remote2 = player2.getBasicRemote();
//                remote2.sendText(score);
//            }
//        } catch (IOException e) {
//// Ignore exception, the score is updated next time and is also displayed on the game screen
//        }
//    }

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
