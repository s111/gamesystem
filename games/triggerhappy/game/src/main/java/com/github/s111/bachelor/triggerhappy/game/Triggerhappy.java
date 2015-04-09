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

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    private static final int ENEMY_WIDTH = 200;
    private static final int ENEMY_HEIGHT = 200;

    public static final int MAX_SCORE = 15;

    private GameSession gameSession;
    private float time = 0;

    private Rectangle topLeft, topMiddle, topRight, botLeft, botMiddle, botRight;

    private Rectangle enemy;
    private boolean enemyAlive = false;
    private int enemyPosition = 0;

    private TrueTypeFont ttfFont;

    private GameSession.Player winner;

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

        Font font = new Font("Arial", Font.BOLD, 48);
        ttfFont = new TrueTypeFont(font, true);

        initiateSpawnPoints();
    }

    public void initiateSpawnPoints() {
        int topY = HEIGHT / 4 - ENEMY_HEIGHT / 2;
        int botY = HEIGHT - ENEMY_HEIGHT * 2;
        int x1 = WIDTH / 4 - ENEMY_WIDTH / 2;
        int x2 = WIDTH / 2 - ENEMY_WIDTH / 2;
        int x3 = WIDTH * 3 / 4 - ENEMY_WIDTH / 2;

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

        winner = gameSession.gameover();

        if (winner == null) {
            if (time / 1000 >= 2) {
                spawnEnemy();
                time = 0;
            }
        }
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

    private void resetEnemy() {
        enemy.setSize(0, 0);
        enemyAlive = false;
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        g.setColor(new Color(44, 62, 89));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(new Color(231, 76, 60));
        g.fill(enemy);

        if (winner != null) {
            drawWinScreen(g);
        }
    }

    private void drawWinScreen(Graphics g) {
        g.setFont(ttfFont);

        String text = "The winner is " + winner.getUsername();
        int textWidth = ttfFont.getWidth(text);
        int textHeight = ttfFont.getLineHeight();

        g.drawString(text, (WIDTH - textWidth) / 2, (HEIGHT - textHeight) / 2);
    }
}