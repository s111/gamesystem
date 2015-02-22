package com.github.s111.bachelor.pong;

import com.github.s111.bachelor.pong.game.Pong;
import com.github.s111.bachelor.pong.network.GameSession;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import javax.websocket.DeploymentException;

public class Application {
    private static Pong game;
    private static GameSession gameSession;

    private Application() {
        game = new Pong("Pong");
        createGameSession();
        startGame();
    }

    public static GameSession getGameSession() {
        return gameSession;
    }

    public static void fatalError(String error) {
        System.out.println(error);
        exit();
    }

    private static void exit() {
        System.out.println("Unable to recover; exiting...");
        System.exit(1);
    }

    private void createGameSession() {
        try {
            gameSession = new GameSession(game);
            gameSession.connect();
        } catch (Exception e) {
            fatalError("Could not start websocket server: " + e.getMessage());
        }
    }

    private void startGame() {
        try {
            AppGameContainer app = new AppGameContainer(game);
            app.setAlwaysRender(true);
            app.start();
        } catch (SlickException e) {
            fatalError("Could not start pong: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Application();
    }
}