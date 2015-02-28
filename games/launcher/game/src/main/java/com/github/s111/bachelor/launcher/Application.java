package com.github.s111.bachelor.launcher;

import com.github.s111.bachelor.launcher.game.Launcher;
import com.github.s111.bachelor.launcher.network.GameSession;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import java.awt.*;

public class Application {
    private static Launcher game;
    private static GameSession gameSession;

    private Application() {
        game = new Launcher("Launcher");
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

    public static void main(String[] args) {
        new Application();
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
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();

        System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");

        try {
            Display.setResizable(false);

            AppGameContainer app = new AppGameContainer(game);
            app.setDisplayMode(width, height, false);
            app.setMouseGrabbed(true);
            app.setAlwaysRender(true);
            app.start();
        } catch (SlickException e) {
            fatalError("Could not start launcher: " + e.getMessage());
        }
    }
}