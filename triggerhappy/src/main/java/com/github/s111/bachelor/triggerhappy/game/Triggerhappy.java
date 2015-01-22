package com.github.s111.bachelor.triggerhappy.game;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;

public class Triggerhappy extends BasicGame {

    private int width;
    private int height;

    private final int ENEMY_WIDTH = 100;
    private final int ENEMY_HEIGHT = 100;

    private Rectangle topLeft, topMiddle, topRight, botLeft, botMiddle, botRight;

    public Triggerhappy(String title) {
        super(title);
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        width = container.getWidth();
        height = container.getHeight();

        initiateSpawnPoints();
    }

    public void initiateSpawnPoints() {
        int topY = height / 4 - ENEMY_HEIGHT / 2;
        int botY = height - ENEMY_HEIGHT * 2;
        int x1 = width / 4 - ENEMY_WIDTH / 2;
        int x2 = width / 2 - ENEMY_WIDTH / 2;
        int x3 = width * 3 /4 - ENEMY_WIDTH / 2;

        topLeft = new Rectangle(x1, topY, ENEMY_WIDTH, ENEMY_HEIGHT);
        topMiddle = new Rectangle(x2, topY, ENEMY_WIDTH, ENEMY_HEIGHT);
        topRight = new Rectangle(x3, topY, ENEMY_WIDTH, ENEMY_HEIGHT);

        botLeft = new Rectangle(x1, botY, ENEMY_WIDTH, ENEMY_HEIGHT);
        botMiddle = new Rectangle(x2, botY, ENEMY_WIDTH, ENEMY_HEIGHT);
        botRight = new Rectangle(x3, botY, ENEMY_WIDTH, ENEMY_HEIGHT);
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
        Input input = container.getInput();

        if (input.isKeyPressed(Input.KEY_Q)) {
            container.exit();
        }

        shootEnemy(input, delta);
    }

    private void shootEnemy(Input input, int delta) {
        if (input.isKeyDown(Input.KEY_NUMPAD1)) {
            botLeft.setSize(0, 0);
        }

        if (input.isKeyDown(Input.KEY_NUMPAD2)) {
            botMiddle.setSize(0, 0);
        }

        if (input.isKeyDown(Input.KEY_NUMPAD3)) {
            botRight.setSize(0, 0);
        }

        if (input.isKeyDown(Input.KEY_NUMPAD4)) {
            topLeft.setSize(0, 0);
        }

        if (input.isKeyDown(Input.KEY_NUMPAD5)) {
            topMiddle.setSize(0, 0);
        }

        if (input.isKeyDown(Input.KEY_NUMPAD6)) {
            topRight.setSize(0, 0);
        }
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        g.setColor(new Color(44, 62, 89));
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(231, 76, 60));
        g.fill(topLeft);
        g.fill(topMiddle);
        g.fill(topRight);

        g.fill(botLeft);
        g.fill(botMiddle);
        g.fill(botRight);
    }
}