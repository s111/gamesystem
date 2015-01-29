package com.github.s111.bachelor.frontend.launcher;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Rectangle;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class Launcher extends BasicGame {

    private static int MARGIN;
    private int screenWidth;
    private int screenHeight;
    private Rectangle background, header, startButton, screenshot;

    private List<String> gameList;

    private Color backgroundColor, headerColor, activeColor, selectedColor, borderColor;

    private Font headerFont, listFont, selectedGameFont, startButtonFont;
    private TrueTypeFont headerTTFont, listTTFont, selectedGameTTFont, startButtonTTFont;

    private Line middleSeperator;


    public Launcher(String title) {
        super(title);
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        screenWidth = container.getWidth();
        screenHeight = container.getHeight();

        MARGIN = screenWidth / 18;

        instantiateColors();
        instantiateFonts();
        instantiateGUI();

        createGameList();
    }

    private void instantiateFonts() {
        headerFont = new Font("Arial", Font.BOLD, 60);
        headerTTFont = new TrueTypeFont(headerFont, true);

        listFont = new Font("Arial", Font.ROMAN_BASELINE, 36);
        listTTFont = new TrueTypeFont(listFont, true);

        selectedGameFont = new Font("Arial", Font.ROMAN_BASELINE, 48);
        selectedGameTTFont = new TrueTypeFont(selectedGameFont, true);

        startButtonFont = new Font("Arial", Font.BOLD, 48);
        startButtonTTFont = new TrueTypeFont(startButtonFont, true);
    }

    private void createGameList() {
        gameList = new ArrayList<String>();
        gameList.add("Pong");
        gameList.add("Scorched Earth");
        gameList.add("TriggerHappy");
        gameList.add("Quizzer");
    }

    private void instantiateColors() {
        activeColor = new Color(225, 30, 45);
        backgroundColor = new Color(21, 22, 24);
        headerColor = new Color(10, 10, 10);
        selectedColor = new Color(26, 28, 30);
        borderColor = new Color(37, 41, 44);
    }

    private void instantiateGUI() {
        background = new Rectangle(0, 0, screenWidth, screenHeight);
        header = new Rectangle(0, 0, screenWidth, screenHeight / 6);
        middleSeperator = new Line(screenWidth * 2 / 5, header.getMaxY(), screenWidth * 2 / 5, screenHeight);
        startButton = new Rectangle(MARGIN, screenHeight - MARGIN - header.getHeight(), middleSeperator.getX() - 2 * MARGIN, header.getHeight());

        screenshot = new Rectangle(MARGIN, 5 / 2 * header.getHeight() - MARGIN, middleSeperator.getX() - 2 * MARGIN, header.getHeight());
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {

    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        drawElements(g);
        drawText(g);
        drawScreenshot(g);
    }

    private void drawScreenshot(Graphics g) {
        g.setColor(Color.black);
        float screenshotWidth = middleSeperator.getX() - 2 * MARGIN;
        g.fillRect(MARGIN, 5 / 2 * header.getHeight(), screenshotWidth, screenshotWidth * 9 / 16);
    }

    private void drawElements(Graphics g) {
        g.setColor(backgroundColor);
        g.fill(background);

        g.setColor(borderColor);
        g.setLineWidth(4);
        g.draw(middleSeperator);

        g.setColor(headerColor);
        g.fill(header);
    }

    private void drawText(Graphics g) {
        drawHeader(g);
        drawGameList(g);
        drawSelectedGameTitle(g);
        drawStartButton(g);
    }

    private void drawStartButton(Graphics g) {
        g.setColor(activeColor);
        g.fill(startButton);
        g.setFont(startButtonTTFont);
        g.setColor(Color.white);

        String startButtonText = "START GAME";
        int startGameTextWidth = startButtonTTFont.getWidth(startButtonText);
        g.drawString(startButtonText, startButton.getCenterX() - startGameTextWidth / 2, startButton.getCenterY() - startButtonTTFont.getHeight() / 2);
    }

    private void drawSelectedGameTitle(Graphics g) {
        String selectedGame = "Pong";
        selectedGame = selectedGame.toUpperCase();
        int selectedGameFontWidth = headerTTFont.getWidth(selectedGame);
        g.setFont(selectedGameTTFont);
        g.drawString(selectedGame, middleSeperator.getX() / 2 - selectedGameFontWidth / 2, 5 / 2 * header.getHeight() - MARGIN);
    }

    private void drawGameList(Graphics g) {
        for (int i = 0; i < gameList.size(); i++) {
            g.setFont(listTTFont);
            g.drawString(gameList.get(i), middleSeperator.getX() + MARGIN / 2, 2 * header.getHeight() + i * 2 * listTTFont.getHeight());
        }
    }

    private void drawHeader(Graphics g) {
        String headerText = "CHOOSE YOUR GAME!";

        g.setColor(Color.white);
        g.setFont(headerTTFont);
        g.drawString(headerText, MARGIN, header.getCenterY() - headerFont.getSize() / 2);
    }
}
