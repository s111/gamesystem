package com.github.s111.bachelor.launcher.network;

import com.github.s111.bachelor.launcher.game.Launcher;
import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;

public class GameSession {
    private final Launcher game;

    public GameSession(Launcher game) throws DeploymentException {
        this.game = game;
        Server server = new Server("localhost", 1234, "/", null, WebsocketServer.class);
        server.start();
    }

    public void onOpen(Session session) throws IOException {
        RemoteEndpoint.Basic remote = session.getBasicRemote();
        remote.sendPing(ByteBuffer.wrap("".getBytes()));
    }

    public void onMessage(Session session, String message) throws IOException {
    }
}
