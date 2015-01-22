package com.github.s111.bachelor.triggerhappy.game;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;

import java.util.Random;

import java.awt.Font;

public class Triggerhappy extends BasicGame {

    private int width;
    private int height;

    private final int ENEMY_WIDTH = 100;
    private final int ENEMY_HEIGHT = 100;

    private float time = 0;

    private Rectangle topLeft, topMiddle, topRight, botLeft, botMiddle, botRight;

    private Rectangle enemy;
    private boolean enemyAlive = false;

    private int player1Score;

    private Font awtFont;
    private TrueTypeFont font;

    public Triggerhappy(String title) {
        super(title);
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        awtFont = new Font(Font.MONOSPACED, Font.BOLD, 24);
        font = new TrueTypeFont(awtFont, true);

        width = container.getWidth();
        height = container.getHeight();

        initiateSpawnPoints();
        resetScore();
    }

    public void initiateSpawnPoints() {
        int topY = height / 4 - ENEMY_HEIGHT / 2;
        int botY = height - ENEMY_HEIGHT * 2;
        int x1 = width / 4 - ENEMY_WIDTH / 2;
        int x2 = width / 2 - ENEMY_WIDTH / 2;
        int x3 = width * 3 / 4 - ENEMY_WIDTH / 2;

        topLeft = new Rectangle(x1, topY, ENEMY_WIDTH, ENEMY_HEIGHT);
        topMiddle = new Rectangle(x2, topY, ENEMY_WIDTH, ENEMY_HEIGHT);
        topRight = new Rectangle(x3, topY, ENEMY_WIDTH, ENEMY_HEIGHT);

        botLeft = new Rectangle(x1, botY, ENEMY_WIDTH, ENEMY_HEIGHT);
        botMiddle = new Rectangle(x2, botY, ENEMY_WIDTH, ENEMY_HEIGHT);
        botRight = new Rectangle(x3, botY, ENEMY_WIDTH, ENEMY_HEIGHT);

        spawnEnemy();
    }

    private void resetScore() {
        player1Score = 0;
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
        time += delta;
        Input input = container.getInput();

        if (input.isKeyPressed(Input.KEY_Q)) {
            container.exit();
        }

        if (time / 1000 >= 2) {
            spawnEnemy();
            time = 0;
        }

        shootEnemy(input, delta);
    }

    private void spawnEnemy() {
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
    }

    private void generateEnemy(Rectangle nextSpawn) {
        enemy = new Rectangle(nextSpawn.getX(), nextSpawn.getY(), ENEMY_WIDTH, ENEMY_HEIGHT);
        enemyAlive = true;
    }


    private void shootEnemy(Input input, int delta) {
        if (enemyAlive) {
            if (input.isKeyDown(Input.KEY_NUMPAD1)) {
                if (hit(botLeft)) {
                    score();
                }
            } else if (input.isKeyDown(Input.KEY_NUMPAD2)) {
                if (hit(botMiddle)) {
                    score();
                }
            } else if (input.isKeyDown(Input.KEY_NUMPAD3)) {
                if (hit(botRight)) {
                    score();
                }
            } else if (input.isKeyDown(Input.KEY_NUMPAD4)) {
                if (hit(topLeft)) {
                    score();
                }
            } else if (input.isKeyDown(Input.KEY_NUMPAD5)) {
                if (hit(topMiddle)) {
                    score();
                }
            } else if (input.isKeyDown(Input.KEY_NUMPAD6)) {
                if (hit(topRight)) {
                    score();
                }
            }
        }
    }

    private void score() {
        enemy.setSize(0, 0);
        player1Score++;
        enemyAlive = false;
    }

    private boolean hit(Rectangle hit) {
        return (enemy.getX() == hit.getX() && enemy.getY() == hit.getY());
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        g.setColor(new Color(44, 62, 89));
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(231, 76, 60));
        g.fill(enemy);

        drawScore(g);
    }

    private void drawScore(Graphics g) {
        g.setFont(font);
        g.drawString("" + player1Score, 30, 30);
    }

}