package com.github.s111.bachelor.launcher.network;

import com.github.s111.bachelor.launcher.game.Launcher;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.server.Server;

import javax.json.*;
import javax.websocket.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private final Launcher game;

    private Session backend;

    private List<String> games = new ArrayList<>();

    public GameSession(Launcher game) throws DeploymentException {
        this.game = game;
        Server server = new Server("localhost", 1234, "/", null, WebsocketServer.class);
        server.start();

        sendReady();
    }

    public void onOpen(Session session) throws IOException {
        RemoteEndpoint.Basic remote = session.getBasicRemote();
        remote.sendPing(ByteBuffer.wrap("".getBytes()));
    }

    public void onMessage(Session session, String message) throws IOException {
    }

    private void sendReady() {
        ClientManager client = ClientManager.createClient();
        try {
            client.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    backend = session;

                    backend.addMessageHandler(new MessageHandler.Whole<String>() {

                        @Override
                        public void onMessage(String message) {
                            if (message.equals("ready")) {
                                return;
                            }

                            JsonReader jsonReader = Json.createReader(new StringReader(message));
                            JsonArray array = jsonReader.readArray();
                            jsonReader.close();

                            for (JsonValue game : array) {
                                games.add(((JsonObject) game).getString("Name"));
                            }
                        }
                    });

                    JsonObject ready = Json.createObjectBuilder()
                            .add("action", "ready")
                            .build();

                    backend.getAsyncRemote().sendObject(ready);
                }
            }, new URI("ws://localhost:3001/ws"));
        } catch (Exception e) {
            System.out.println("Unable to recover; exiting...");
            System.exit(1);
        }
    }

    public List<String> getGames() {
        return games;
    }
}
