package com.github.s111.bachelor.pong;

import com.github.s111.bachelor.pong.network.GameSession;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

public class Pong extends BasicGame {
    private GameSession gameSession = Application.getGameSession();

    private String message;

    public Pong(String title) {
        super(title);
    }

    public void showMessage(String message) {
        this.message = message;
    }

    @Override
    public void init(GameContainer container) throws SlickException {

    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {

    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        if (message != null) {
            g.drawString(message, 32, 32);
        }
    }
}