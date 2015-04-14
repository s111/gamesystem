package com.github.s111.bachelor.pong.game;

import com.github.s111.bachelor.pong.Application;
import com.github.s111.bachelor.pong.network.GameSession;
import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import java.awt.Font;

public class Pong extends BasicGame {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    private static final int MARGIN = 32;
    private static final int PADDLE_WIDTH = 32;
    private static final int PADDLE_HEIGHT = 128;
    private static final int BALL_RADIUS = 8;
    private static final float BALL_SPEED = 1.0f;
    private static final double MAX_BOUNCE_ANGLE = 5 * Math.PI / 12;

    private static final Color RED = new Color(255, 3, 115);
    private static final Color GREEN = new Color(0, 252, 140);
    private static final Color BLUE = new Color(0, 156, 255);
    private static final Color YELLOW = new Color(252, 255, 2);
    private static final Color BLACK = new Color(17, 18, 19);
    private static final Color WHITE = new Color(238, 239, 239);

    private int left;
    private int right;

    private int verticalCenter;
    private int horizontalCenter;

    private GameSession gameSession;

    private Rectangle player1;
    private Rectangle player2;

    private Circle ball;

    private Vector2f ballSpeed;

    private int ballHorizontalDirection;
    private int ballVerticalDirection;

    private int player1Score;
    private int player2Score;

    private boolean gameover;

    private Font scoreFont;
    private Font winFont;
    private TrueTypeFont scoreTTF;
    private TrueTypeFont winTTF;

    public Pong(String title) {
        super(title);
    }

    public void movePlayer1(float position) {
        movePlayer(player1, position);
    }

    public void movePlayer2(float position) {
        movePlayer(player2, position);
    }

    private void movePlayer(Rectangle player, float position) {
        if (position < 0 || position > 1) {
            return;
        }

        player.setCenterY(MARGIN + position * (HEIGHT - MARGIN * 2));
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        gameSession = Application.getGameSession();

        scoreFont = new Font(Font.MONOSPACED, Font.BOLD, 24);
        winFont = new Font(Font.MONOSPACED, Font.BOLD, 48);
        scoreTTF = new TrueTypeFont(scoreFont, true);
        winTTF = new TrueTypeFont(winFont, true);

        setBoundaries(container);
        instantiatePlayers();
        instantiateBall();
        resetScore();
    }

    private void setBoundaries(GameContainer container) {
        left = MARGIN + PADDLE_WIDTH;
        right = WIDTH - MARGIN - PADDLE_WIDTH;

        verticalCenter = HEIGHT / 2;
        horizontalCenter = WIDTH / 2;
    }

    private void instantiatePlayers() {
        player1 = new Rectangle(MARGIN, 0, PADDLE_WIDTH, PADDLE_HEIGHT);
        player1.setCenterY(verticalCenter);

        player2 = new Rectangle(right, 0, PADDLE_WIDTH, PADDLE_HEIGHT);
        player2.setCenterY(verticalCenter);
    }

    private void instantiateBall() {
        ball = new Circle(horizontalCenter, verticalCenter, BALL_RADIUS);

        resetBall();
    }

    private void resetScore() {
        player1Score = 0;
        player2Score = 0;
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
        Input input = container.getInput();

        if (input.isKeyPressed(Input.KEY_Q)) {
            container.exit();
        }

        gameover = player1Score >= 11 || player2Score >= 11;

        if (!gameover) {
            movePaddles(input, delta);
            checkBallCollision();
            moveBall(delta);
        }
    }

    private void movePaddles(Input input, int delta) {
        if (input.isKeyDown(Input.KEY_W)) {
            player1.setCenterY(player1.getCenterY() - 0.5f * delta);
        } else if (input.isKeyDown(Input.KEY_S)) {
            player1.setCenterY(player1.getCenterY() + 0.5f * delta);
        }

        if (input.isKeyDown(Input.KEY_UP)) {
            player2.setCenterY(player2.getCenterY() - 0.5f * delta);
        } else if (input.isKeyDown(Input.KEY_DOWN)) {
            player2.setCenterY(player2.getCenterY() + 0.5f * delta);
        }
    }

    private void checkBallCollision() {
        double bounceAngle = calculateBounceAngle();

        boolean ballHittingRight = ball.getMaxX() > right && ballHorizontalDirection != -1;
        boolean ballHittingLeft = ball.getX() < left && ballHorizontalDirection != 1;
        boolean ballHittingTop = ball.getY() < MARGIN && ballVerticalDirection != 1;
        boolean ballHittingBottom = ball.getMaxY() > HEIGHT - MARGIN && ballVerticalDirection != -1;
        boolean ballHittingPlayer1 = ball.getMaxY() > player1.getY() && ball.getY() < player1.getMaxY() && ballHorizontalDirection == -1;
        boolean ballHittingPlayer2 = ball.getMaxY() > player2.getY() && ball.getY() < player2.getMaxY() && ballHorizontalDirection == 1;

        if (ballHittingTop || ballHittingBottom) {
            bounceBallOfWall();
        }

        if (ballHittingLeft || ballHittingRight) {
            if (ballHittingPlayer1 || ballHittingPlayer2) {
                bounceBallOfPaddle(bounceAngle);
            } else {
                if (ballHittingLeft) {
                    player2Score++;
                } else {
                    player1Score++;
                }

                resetBall();
            }
        }
    }

    private void moveBall(int delta) {
        ball.setCenterX(ballSpeed.getX() * delta + ball.getCenterX());
        ball.setCenterY(ballSpeed.getY() * delta + ball.getCenterY());
    }

    private double calculateBounceAngle() {
        float ballPositionRelativeToPaddleCenter;

        if (ballSpeed.getX() > 0) {
            ballPositionRelativeToPaddleCenter = player2.getCenterY() - ball.getCenterY();
        } else {
            ballPositionRelativeToPaddleCenter = player1.getCenterY() - ball.getCenterY();
        }

        float normalizedRelativeBallPosition = ballPositionRelativeToPaddleCenter / (PADDLE_HEIGHT / 2);
        double bounceAngle = normalizedRelativeBallPosition * MAX_BOUNCE_ANGLE;

        bounceAngle = Math.max(Math.min(bounceAngle, MAX_BOUNCE_ANGLE), -MAX_BOUNCE_ANGLE);

        return bounceAngle;
    }

    private void bounceBallOfPaddle(double bounceAngle) {
        ballVerticalDirection = ballSpeed.getY() > 0 ? 1 : -1;

        if (ballSpeed.getY() == 0) {
            ballVerticalDirection = bounceAngle > 0 ? -1 : 1;
        }

        ballHorizontalDirection = -ballHorizontalDirection;

        float horizontalBallSpeed = (float) (BALL_SPEED * Math.cos(bounceAngle));
        float verticalBallSpeed = (float) Math.abs(BALL_SPEED * Math.sin(bounceAngle));

        ballSpeed.set(ballHorizontalDirection * horizontalBallSpeed, ballVerticalDirection * verticalBallSpeed);
    }

    private void bounceBallOfWall() {
        ballVerticalDirection = -ballVerticalDirection;

        ballSpeed.set(ballSpeed.getX(), -ballSpeed.getY());
    }

    private void resetBall() {
        ball.setCenterX(horizontalCenter);
        ball.setCenterY(verticalCenter);

        ballHorizontalDirection = Math.random() > 0.5 ? 1 : -1;
        ballVerticalDirection = 0;

        ballSpeed = new Vector2f(ballHorizontalDirection * BALL_SPEED, ballVerticalDirection);
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        g.setFont(scoreTTF);

        g.setBackground(BLACK);
        g.clear();

        g.setColor(WHITE);
        drawScore(g);

        if (!gameover) {
            g.setColor(RED);
            g.fill(player1);

            g.setColor(GREEN);
            g.fill(player2);

            g.setColor(YELLOW);
            g.fill(ball);

            g.setColor(BLUE);
            g.setLineWidth(4);
            g.drawLine(horizontalCenter, 0, horizontalCenter, HEIGHT);
        } else {
            String message = (player1Score > player2Score ? gameSession.getPlayer1Username() : gameSession.getPlayer2Username()) + " wins!";

            int messageWidth = winTTF.getWidth(message);
            int messageHeight = winTTF.getHeight(message);

            g.setFont(winTTF);
            g.setColor(WHITE);
            g.drawString(message, (WIDTH - messageWidth) / 2, (HEIGHT - messageHeight) / 2);
        }
    }

    private void drawScore(Graphics g) {
        String score1 = gameSession.getPlayer1Username() + ": " + player1Score;
        String score2 = gameSession.getPlayer2Username() + ": " + player2Score;

        int score1Width = scoreTTF.getWidth(score1);

        g.drawString(score1, horizontalCenter - score1Width - MARGIN * 2, MARGIN);
        g.drawString(score2, horizontalCenter + MARGIN * 2, MARGIN);
    }
}