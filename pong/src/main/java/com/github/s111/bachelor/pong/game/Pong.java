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
    private static final double MAX_BOUNCE_ANGLE = Math.PI / 3;

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

        ballHorizontalDirection = Math.random() > 0.5 ? 1 : -1;
        ballVerticalDirection = 0;
        ballSpeed = new Vector2f(ballHorizontalDirection * BALL_SPEED, ballVerticalDirection);
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
        Input input = container.getInput();

        if (input.isKeyDown(Input.KEY_W)) {
            player1.setCenterY(player1.getCenterY() - 0.25f);
        } else if (input.isKeyDown(Input.KEY_S)) {
            player1.setCenterY(player1.getCenterY() + 0.25f);
        }

        player2.setCenterY(ball.getCenterY());

        float ballPositionRelativeToPaddleCenter;

        if (ballSpeed.getX() > 0) {
            ballPositionRelativeToPaddleCenter = player2.getCenterY() - ball.getCenterY();
        } else {
            ballPositionRelativeToPaddleCenter = player1.getCenterY() - ball.getCenterY();
        }

        float normalizedRelativeBallPosition = ballPositionRelativeToPaddleCenter / (PADDLE_HEIGHT / 2);
        double bounceAngle = normalizedRelativeBallPosition * MAX_BOUNCE_ANGLE;

        bounceAngle = Math.max(Math.min(bounceAngle, MAX_BOUNCE_ANGLE), -MAX_BOUNCE_ANGLE);

        float ballRight = ball.getCenterX() + BALL_RADIUS;
        float ballLeft = ball.getCenterX() - BALL_RADIUS;

        boolean ballHittingRightPaddle = ballRight > right && ballHorizontalDirection != -1;
        boolean ballHittingLeftPaddle = ballLeft < left && ballHorizontalDirection != 1;
        boolean ballHittingTop = ball.getCenterY() - BALL_RADIUS < MARGIN && ballVerticalDirection != 1;
        boolean ballHittingBottom = ball.getCenterY() + BALL_RADIUS > height - MARGIN && ballVerticalDirection != -1;

        if (ballHittingTop || ballHittingBottom) {
            ballVerticalDirection = -ballVerticalDirection;

            ballSpeed.set(ballSpeed.getX(), -ballSpeed.getY());
        }

        if (ballHittingLeftPaddle || ballHittingRightPaddle) {
            ballVerticalDirection = ballSpeed.getY() > 0 ? 1 : -1;

            if (ballSpeed.getY() == 0) {
                ballVerticalDirection = normalizedRelativeBallPosition > 0 ? -1 : 1;
            }

            ballHorizontalDirection = -ballHorizontalDirection;
            ballSpeed.set(ballHorizontalDirection * ((float) (BALL_SPEED * Math.cos(bounceAngle))), ballVerticalDirection * (float) Math.abs(BALL_SPEED * Math.sin(bounceAngle)));
        }

        ball.setCenterX(ballSpeed.getX() + ball.getCenterX());
        ball.setCenterY(ballSpeed.getY() + ball.getCenterY());
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        g.fill(player1);
        g.fill(player2);

        g.fill(ball);

        g.drawLine(horizontalCenter, 0, horizontalCenter, height);
    }
}