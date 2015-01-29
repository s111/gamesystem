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
    private Rectangle background, header, startButton, selectedBox, screenshot;

    private List<String> gameList;

    private Color backgroundColor, headerColor, activeColor, selectedColor, borderColor;

    private Font headerFont, listFont, selectedGameFont, startButtonFont;
    private TrueTypeFont headerTTFont, listTTFont, selectedGameTTFont, startButtonTTFont;

    private Line middleSeperator;

    private String selectedGameName;
    private int selectedGameNr = 0;

    private float gameListX, gameListY, gameListWidth;
    private float selectedBoxStartingY;


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
        headerFont = new Font("Arial", Font.BOLD, screenHeight / 20);
        headerTTFont = new TrueTypeFont(headerFont, true);

        listFont = new Font("Arial", Font.ROMAN_BASELINE, screenHeight / 30);
        listTTFont = new TrueTypeFont(listFont, true);

        selectedGameFont = new Font("Arial", Font.ROMAN_BASELINE, screenHeight / 23);
        selectedGameTTFont = new TrueTypeFont(selectedGameFont, true);

        startButtonFont = new Font("Arial", Font.BOLD, screenHeight / 23);
        startButtonTTFont = new TrueTypeFont(startButtonFont, true);
    }

    private void createGameList() {
        gameList = new ArrayList<String>();
        gameList.add("Pong");
        gameList.add("Scorched Earth");
        gameList.add("TriggerHappy");
        gameList.add("Quizzer");
        gameList.add("Kebab");
    }

    private void instantiateColors() {
        activeColor = new Color(225, 30, 45);
        backgroundColor = new Color(21, 22, 24);
        headerColor = new Color(10, 10, 10);
        selectedColor = new Color(60, 60, 60);
        borderColor = new Color(37, 41, 44);
    }

    private void instantiateGUI() {
        background = new Rectangle(0, 0, screenWidth, screenHeight);
        header = new Rectangle(0, 0, screenWidth, screenHeight / 6);
        middleSeperator = new Line(screenWidth * 2 / 5, header.getMaxY(), screenWidth * 2 / 5, screenHeight);
        startButton = new Rectangle(MARGIN, screenHeight - MARGIN - header.getHeight(), middleSeperator.getX() - 2 * MARGIN, header.getHeight());

        instantiateSelectionBox();

        float screenshotWidth = middleSeperator.getX() - 2 * MARGIN;
        screenshot = new Rectangle(MARGIN, 5 / 2 * header.getHeight(), screenshotWidth, screenshotWidth * 9 / 16);
    }

    private void instantiateSelectionBox() {
        gameListX = middleSeperator.getX() + MARGIN / 2;
        gameListY = 2 * header.getHeight();
        gameListWidth = screenWidth - middleSeperator.getX();
        selectedBoxStartingY = gameListY + listTTFont.getLineHeight() / 2;
        selectedBox = new Rectangle(gameListX - MARGIN / 2, selectedBoxStartingY, gameListWidth, listTTFont.getLineHeight() * 2);
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
        Input input = container.getInput();

        if (input.isKeyPressed(Input.KEY_Q)) {
            container.exit();
        }

        moveSelection(input, delta);
    }

    private void moveSelection(Input input, int delta) {
        if (input.isKeyDown(Input.KEY_UP)) {
            if (selectedGameNr == 0) {
                selectedGameNr = gameList.size() - 1;
            } else {
                selectedGameNr--;
            }
        } else if (input.isKeyDown(Input.KEY_DOWN)) {
            if (selectedGameNr == gameList.size() - 1) {
                selectedGameNr = 0;
            } else {
                selectedGameNr++;
            }
        }
        selectedBox.setCenterY(selectedBoxStartingY + selectedGameNr * selectedBox.getHeight());
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        drawBackground(g);
        drawSelectedBox(g);
        drawBorder(g);
        drawText(g);
        drawScreenshot(g);
    }

    private void drawBorder(Graphics g) {
        g.setColor(borderColor);
        g.setLineWidth(screenWidth / 160);
        g.draw(middleSeperator);
    }

    private void drawScreenshot(Graphics g) {
        g.setColor(Color.black);
        g.fill(screenshot);
    }

    private void drawBackground(Graphics g) {
        g.setColor(backgroundColor);
        g.fill(background);

        g.setColor(headerColor);
        g.fill(header);
    }

    private void drawSelectedBox(Graphics g) {
        g.setColor(selectedColor);
        g.fill(selectedBox);

        g.setLineWidth(screenWidth / 160);
        g.setColor(borderColor);
        g.draw(selectedBox);
    }

    private void drawText(Graphics g) {
        drawHeader(g);
        drawSelectedGameTitle(g);
        drawGameList(g);
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
        selectedGameName = gameList.get(selectedGameNr);
        selectedGameName = selectedGameName.toUpperCase();
        int selectedGameFontWidth = selectedGameTTFont.getWidth(selectedGameName);
        g.setFont(selectedGameTTFont);
        g.drawString(selectedGameName, middleSeperator.getX() / 2 - selectedGameFontWidth / 2, 5 / 2 * header.getHeight() - MARGIN);
    }

    private void drawGameList(Graphics g) {
        for (int i = 0; i < gameList.size(); i++) {
            if (selectedGameName.equals(gameList.get(i).toUpperCase())) {
                g.setColor(activeColor);
            }
            g.setFont(listTTFont);
            g.drawString(gameList.get(i), gameListX, gameListY + i * 2 * listTTFont.getHeight());
            g.setColor(Color.white);
        }
    }

    private void drawHeader(Graphics g) {
        String headerText = "CHOOSE YOUR GAME!";

        g.setColor(Color.white);
        g.setFont(headerTTFont);
        g.drawString(headerText, MARGIN, header.getCenterY() - headerFont.getSize() / 2);
    }
}
