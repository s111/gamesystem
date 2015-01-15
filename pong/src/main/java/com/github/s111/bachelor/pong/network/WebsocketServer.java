package com.github.s111.bachelor.pong.network;

import com.github.s111.bachelor.pong.Application;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "/ws")
public class WebsocketServer {
    private GameSession gameSession = Application.getGameSession();

    @OnOpen
    public void onOpen(Session session) {
        gameSession.onOpen(session);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        gameSession.onMessage(session, message);
    }
}
