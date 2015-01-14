package com.github.s111.bachelor.pong.server;

import com.github.s111.bachelor.pong.Pong;

import javax.websocket.OnMessage;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws")
public class WebsocketServer {
    @OnMessage
    public void onMessage(String message) {
        Pong.getGame().showMessage(message);
    }
}
