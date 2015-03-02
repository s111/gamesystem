package com.github.s111.bachelor.pong.game;

import com.github.s111.bachelor.pong.Application;
import com.github.s111.bachelor.pong.network.GameSession;
import org.newdawn.slick.*;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import java.awt.Font;

public class Pong extends BasicGame {
    private static final int MARGIN = 32;
    private static final int PADDLE_WIDTH = 32;
    private static final int PADDLE_HEIGHT = 128;
    private static final int BALL_RADIUS = 8;
    private static final float BALL_SPEED = 1.0f;
    private static final double MAX_BOUNCE_ANGLE = 5 * Math.PI / 12;

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

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

    private Font awtFont;
    private TrueTypeFont font;

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

        awtFont = new Font(Font.MONOSPACED, Font.BOLD, 48);
        font = new TrueTypeFont(awtFont, true);

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

        movePaddles(input, delta);
        checkBallCollision();
        moveBall(delta);
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
        drawScore(g);

        g.fill(player1);
        g.fill(player2);

        g.fill(ball);

        g.drawLine(horizontalCenter, 0, horizontalCenter, HEIGHT);
    }

    private void drawScore(Graphics g) {
        int score1Width = font.getWidth("" + player1Score);
        int score2Width = font.getWidth("" + player2Score);

        g.setFont(font);
        g.drawString("" + player1Score, horizontalCenter - score1Width / 2 - MARGIN * 2, MARGIN);
        g.drawString("" + player2Score, horizontalCenter - score2Width / 2 + MARGIN * 2, MARGIN);
    }
}