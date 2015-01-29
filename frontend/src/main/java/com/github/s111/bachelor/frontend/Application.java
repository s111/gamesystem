package com.github.s111.bachelor.frontend;

import com.github.s111.bachelor.frontend.launcher.Launcher;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

public class Application {
    private static Launcher game;
//    private static GameSession gameSession;
    private Application() {
        game = new Launcher("Pong");
//        createGameSession();
        startGame();
    }
//    public static GameSession getGameSession() {
//        return gameSession;
//    }
    public static void fatalError(String error) {
        System.out.println(error);
        exit();
    }
    private static void exit() {
        System.out.println("Unable to recover; exiting...");
        System.exit(1);
    }
//    private void createGameSession() {
//        try {
//            gameSession = new GameSession(game);
//        } catch (DeploymentException e) {
//            fatalError("Could not start websocket server: " + e.getMessage());
//        }
//    }
    private void startGame() {
        try {
            AppGameContainer app = new AppGameContainer(game);
            app.setAlwaysRender(true);
            app.start();
        } catch (SlickException e) {
            fatalError("Could not start game: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        new Application();
    }
}