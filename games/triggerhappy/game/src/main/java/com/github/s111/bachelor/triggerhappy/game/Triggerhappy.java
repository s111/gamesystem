package com.github.s111.bachelor.triggerhappy.game;

import com.github.s111.bachelor.triggerhappy.Application;
import com.github.s111.bachelor.triggerhappy.network.GameSession;
import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;

import java.awt.Font;
import java.util.Random;

public class Triggerhappy extends BasicGame {

    private final int ENEMY_WIDTH = 100;
    private final int ENEMY_HEIGHT = 100;
    private GameSession gameSession;
    private int width;
    private int height;
    private float time = 0;

    private Rectangle topLeft, topMiddle, topRight, botLeft, botMiddle, botRight;

    private Rectangle enemy;
    private boolean enemyAlive = false;
    private int enemyPosition = 0;

    private Font awtFont;
    private TrueTypeFont font;

    public Triggerhappy(String title) {
        super(title);
    }

    public boolean checkIfHit(int position) {
        if (enemyAlive) {
            if (position == enemyPosition) {
                resetEnemy();
                return true;
            }
        }
        return false;
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        gameSession = Application.getGameSession();

        awtFont = new Font(Font.MONOSPACED, Font.BOLD, 24);
        font = new TrueTypeFont(awtFont, true);

        width = container.getWidth();
        height = container.getHeight();

        initiateSpawnPoints();
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
                generateEnemy(topLeft, 1);
                break;
            case 2:
                generateEnemy(topMiddle, 2);
                break;
            case 3:
                generateEnemy(topRight, 3);
                break;
            case 4:
                generateEnemy(botLeft, 4);
                break;
            case 5:
                generateEnemy(botMiddle, 5);
                break;
            case 6:
                generateEnemy(botRight, 6);
                break;
            default:
        }
    }

    private void generateEnemy(Rectangle nextSpawn, int position) {
        enemy = new Rectangle(nextSpawn.getX(), nextSpawn.getY(), ENEMY_WIDTH, ENEMY_HEIGHT);
        enemyPosition = position;
        enemyAlive = true;
    }


    private void shootEnemy(Input input, int delta) {
        if (enemyAlive) {
            if (input.isKeyDown(Input.KEY_NUMPAD1)) {
                if (hit(botLeft)) {
                    resetEnemy();
                }
            } else if (input.isKeyDown(Input.KEY_NUMPAD2)) {
                if (hit(botMiddle)) {
                    resetEnemy();
                }
            } else if (input.isKeyDown(Input.KEY_NUMPAD3)) {
                if (hit(botRight)) {
                    resetEnemy();
                }
            } else if (input.isKeyDown(Input.KEY_NUMPAD4)) {
                if (hit(topLeft)) {
                    resetEnemy();
                }
            } else if (input.isKeyDown(Input.KEY_NUMPAD5)) {
                if (hit(topMiddle)) {
                    resetEnemy();
                }
            } else if (input.isKeyDown(Input.KEY_NUMPAD6)) {
                if (hit(topRight)) {
                    resetEnemy();
                }
            }
        }
    }

    private void resetEnemy() {
        enemy.setSize(0, 0);
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
    }
}