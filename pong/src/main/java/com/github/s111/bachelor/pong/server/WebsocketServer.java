package com.github.s111.bachelor.pong.server;

import com.github.s111.bachelor.pong.Pong;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "/ws")
public class WebsocketServer {
    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;

        int players = Pong.getPlayers();

        switch (players) {
            case 0: Pong.addPlayer(session); break;
            case 1: Pong.addPlayer(session); break;
            default:
                try {
                    session.getBasicRemote().sendText("Already got 2 players");
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    @OnClose
    public void onClose() {
        Pong.removePlayer();
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        Pong.getGame().showMessage(message);
    }
}
