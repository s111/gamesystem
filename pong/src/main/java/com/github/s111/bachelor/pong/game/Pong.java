package com.github.s111.bachelor.pong.game;

import com.github.s111.bachelor.pong.Application;
import com.github.s111.bachelor.pong.network.GameSession;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

public class Pong extends BasicGame {
    private static final int MARGIN = 32;
    private static final int PADDLE_WIDTH = 32;
    private static final int PADDLE_HEIGHT = 128;
    private static final int BALL_RADIUS = 8;
    private static final float BALL_SPEED = 1.0f;
    private static final double MAX_BOUNCE_ANGLE = 5 * Math.PI / 12;

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

    private Vector2f ballSpeed;

    private int ballHorizontalDirection;
    private int ballVerticalDirection;

    public Pong(String title) {
        super(title);
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        setBoundaries(container);
        instantiatePlayers();
        instantiateBall();
    }

    private void setBoundaries(GameContainer container) {
        width = container.getWidth();
        height = container.getHeight();

        left = MARGIN + PADDLE_WIDTH;
        right = width - MARGIN - PADDLE_WIDTH;

        verticalCenter = height / 2;
        horizontalCenter = width / 2;
    }

    private void instantiatePlayers() {
        player1 = new Rectangle(MARGIN, 0, PADDLE_WIDTH, PADDLE_HEIGHT);
        player1.setCenterY(verticalCenter);

        player2 = new Rectangle(right, 0, PADDLE_WIDTH, PADDLE_HEIGHT);
        player2.setCenterY(verticalCenter + 64);
    }

    private void instantiateBall() {
        ball = new Circle(horizontalCenter, verticalCenter, BALL_RADIUS);

        resetBall();
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
        Input input = container.getInput();

        movePaddles(input, delta);
        checkBallCollision();
        moveBall(delta);
    }

    private void movePaddles(Input input, int delta) {
        if (input.isKeyDown(Input.KEY_W)) {
            player1.setCenterY(player1.getCenterY() - 0.25f * delta);
        } else if (input.isKeyDown(Input.KEY_S)) {
            player1.setCenterY(player1.getCenterY() + 0.25f * delta);
        }

        player2.setCenterY(ball.getCenterY());
    }

    private void checkBallCollision() {
        double bounceAngle = calculateBounceAngle();

        boolean ballHittingRight = ball.getMaxX() > right && ballHorizontalDirection != -1;
        boolean ballHittingLeft = ball.getX() < left && ballHorizontalDirection != 1;
        boolean ballHittingTop = ball.getY() < MARGIN && ballVerticalDirection != 1;
        boolean ballHittingBottom = ball.getMaxY() > height - MARGIN && ballVerticalDirection != -1;
        boolean ballHittingPlayer1 = ball.getMaxY() > player1.getY() && ball.getY() < player1.getMaxY() && ballHorizontalDirection == -1;
        boolean ballHittingPlayer2 = ball.getMaxY() > player2.getY() && ball.getY() < player2.getMaxY() && ballHorizontalDirection == 1;

        if (ballHittingTop || ballHittingBottom) {
            bounceBallOfWall();
        }

        if (ballHittingLeft || ballHittingRight) {
            if (ballHittingPlayer1 || ballHittingPlayer2) {
                bounceBallOfPaddle(bounceAngle);
            } else {
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
        g.fill(player1);
        g.fill(player2);

        g.fill(ball);

        g.drawLine(horizontalCenter, 0, horizontalCenter, height);
    }
}