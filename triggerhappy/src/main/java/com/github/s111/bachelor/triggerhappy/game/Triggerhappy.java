package com.github.s111.bachelor.triggerhappy.game;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;

import java.util.Random;

public class Triggerhappy extends BasicGame {

    private int width;
    private int height;

    private final int ENEMY_WIDTH = 100;
    private final int ENEMY_HEIGHT = 100;

    private float time = 0;

    private Rectangle topLeft, topMiddle, topRight, botLeft, botMiddle, botRight;

    private Rectangle enemy;

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

        spawnEnemy();
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
        time += delta;
        Input input = container.getInput();

        if (input.isKeyPressed(Input.KEY_Q)) {
            container.exit();
        }

        if(time / 1000 >= 2) {
            spawnEnemy();
            time = 0;
        }

        shootEnemy(input, delta);
    }

    private void spawnEnemy() {

        // Random number between 1 and 6
        // Chooses where to spawn
        Random rand = new Random();
        int randomNum = rand.nextInt((6 - 1) + 1) + 1;

        switch (randomNum) {
            case 1:
                generateEnemy(topLeft);
                break;
            case 2:
                generateEnemy(topMiddle);
                break;
            case 3:
                generateEnemy(topRight);
                break;
            case 4:
                generateEnemy(botLeft);
                break;
            case 5:
                generateEnemy(botMiddle);
                break;
            case 6:
                generateEnemy(botRight);
                break;
            default:
        }
        // Spawn the enemy at a random time between 2-5 seconds
    }

    private void generateEnemy(Rectangle nextSpawn) {
        enemy = new Rectangle(nextSpawn.getX(), nextSpawn.getY(), ENEMY_WIDTH, ENEMY_HEIGHT);
    }


    private void shootEnemy(Input input, int delta) {
        if (input.isKeyDown(Input.KEY_NUMPAD1)) {
            if(hit(botLeft)) {
                enemy.setSize(0,0);
            }
        }

        if (input.isKeyDown(Input.KEY_NUMPAD2)) {
            if(hit(botMiddle)) {
                enemy.setSize(0,0);
            }
        }

        if (input.isKeyDown(Input.KEY_NUMPAD3)) {
            if(hit(botRight)) {
                enemy.setSize(0,0);
            }
        }

        if (input.isKeyDown(Input.KEY_NUMPAD4)) {
            if(hit(topLeft)) {
                enemy.setSize(0,0);
            }
        }

        if (input.isKeyDown(Input.KEY_NUMPAD5)) {
            if(hit(topMiddle)) {
                enemy.setSize(0,0);
            }
        }

        if (input.isKeyDown(Input.KEY_NUMPAD6)) {
            if(hit(topRight)) {
                enemy.setSize(0,0);
            }
        }
    }

    private boolean hit(Rectangle hit) {
        return enemy.getX() == hit.getX() && enemy.getY() == hit.getY();
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        g.setColor(new Color(44, 62, 89));
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(231, 76, 60));
        g.fill(enemy);
    }
}