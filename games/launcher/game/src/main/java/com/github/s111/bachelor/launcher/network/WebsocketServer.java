package com.github.s111.bachelor.launcher.network;

import com.github.s111.bachelor.launcher.Application;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;

@ServerEndpoint(value = "/ws")
public class WebsocketServer {
    private GameSession gameSession = Application.getGameSession();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        session.setMaxIdleTimeout(1000);
        gameSession.onOpen(session);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        gameSession.onMessage(session, message);
    }

    @OnMessage
    public void onMessage(Session session, PongMessage message) throws IOException {
        RemoteEndpoint.Basic remote = session.getBasicRemote();
        remote.sendPing(ByteBuffer.wrap("".getBytes()));
    }
}