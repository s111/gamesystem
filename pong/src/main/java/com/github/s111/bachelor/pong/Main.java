package com.github.s111.bachelor.pong;

import org.glassfish.tyrus.server.Server;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

public class Main {
    public static void main(String[] args) {
        try {
            AppGameContainer app = new AppGameContainer(Pong.getGame());
            app.setAlwaysRender(true);
            app.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }
}
