package com.github.s111.bachelor.pong.game;

import com.github.s111.bachelor.pong.Application;
import com.github.s111.bachelor.pong.network.GameSession;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Vector2f;

public class Pong extends BasicGame {
    private static final int MARGIN = 32;
    private static final int PADDLE_WIDTH = 32;
    private static final int PADDLE_HEIGHT = 128;
    private static final int BALL_RADIUS = 8;

    private int width;
    private int height;

    private int left;
    private int right;

    private int verticalCenter;
    private int horizontalCenter;

    private GameSession gameSession = Application.getGameSession();

    private Rectangle player1;
    private Rectangle player2;

    private Circle ball;

    public Pong(String title) {
        super(title);
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        setBoundaries(container);
        instantiatePlayers();

        ball = new Circle(horizontalCenter, verticalCenter, BALL_RADIUS);
    }

    private void setBoundaries(GameContainer container) {
        width = container.getWidth();
        height = container.getHeight();

        left = MARGIN;
        right = width - MARGIN - PADDLE_WIDTH;

        verticalCenter = height / 2;
        horizontalCenter = width / 2;
    }

    private void instantiatePlayers() {
        player1 = new Rectangle(left, 0, PADDLE_WIDTH, PADDLE_HEIGHT);
        player1.setCenterY(verticalCenter);

        player2 = new Rectangle(right, 0, PADDLE_WIDTH, PADDLE_HEIGHT);
        player2.setCenterY(verticalCenter);
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {

    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        g.fill(player1);
        g.fill(player2);

        g.fill(ball);

        g.drawLine(horizontalCenter, 0, horizontalCenter, height);
    }
}