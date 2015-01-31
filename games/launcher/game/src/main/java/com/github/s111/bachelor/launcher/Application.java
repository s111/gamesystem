package com.github.s111.bachelor.launcher;

import com.github.s111.bachelor.launcher.game.Launcher;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

public class Application {
    private static Launcher game;

    private Application() {
        game = new Launcher("Pong");

        startGame();
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

    private void startGame() {
        try {
            AppGameContainer app = new AppGameContainer(game, 640, 480, false);
            app.setAlwaysRender(true);
            app.start();
        } catch (SlickException e) {
            fatalError("Could not start game: " + e.getMessage());
        }
    }
}
