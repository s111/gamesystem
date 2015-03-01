package com.github.s111.bachelor.triggerhappy;

import com.github.s111.bachelor.triggerhappy.game.Triggerhappy;
import com.github.s111.bachelor.triggerhappy.network.GameSession;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import java.awt.*;

public class Application {
    private static Triggerhappy game;
    private static GameSession gameSession;

    private Application() {
        game = new Triggerhappy("TriggerHappy");
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
            app.setTargetFrameRate(60);
            app.setMouseGrabbed(true);
            app.setAlwaysRender(true);
            app.start();
        } catch (SlickException e) {
            fatalError("Could not start triggerhappy: " + e.getMessage());
        }
    }
}