package com.github.s111.bachelor.pong;

import org.glassfish.tyrus.server.Server;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        try {
            AppGameContainer app = new AppGameContainer(new Pong("Pong"));
            app.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
