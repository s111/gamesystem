package com.github.s111.bachelor.quizzer.game;

import com.github.s111.bachelor.quizzer.Application;
import com.github.s111.bachelor.quizzer.network.GameSession;
import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import java.awt.Font;

public class Quizzer extends BasicGame {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    private GameSession gameSession;

    private Font awtFont;
    private TrueTypeFont font;
    private int fontTextHeight;
    private Color[] fontColors;

    private int questionPosX;
    private int questionPosY;
    private int optionsPosX;

    private Question[] questionList;
    private Question currentQuestion;

    public Quizzer(String title) {
        super(title);
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        gameSession = Application.getGameSession();

        awtFont = new Font(Font.MONOSPACED, Font.BOLD, 16);
        font = new TrueTypeFont(awtFont, true);
        fontColors = new Color[]{Color.red, Color.yellow, Color.green, Color.blue};

        initiateQuestions();
        setPositions();
    }

    private void initiateQuestions() {
        Question question1 = new Question("What is the Capital of China?", "Beijing");
        question1.addOptions("Beijing", "Washington", "Storhaug", "Hong Kong");
        Question question2 = new Question("What is the tallest mountain in Norway?", "Galdhøpiggen");
        question2.addOptions("Glittertinden", "Veslpiggen", "Galdhøpiggen", "Sentraltind");
        Question question3 = new Question("How old is the norwegian constitution?", "201");
        question3.addOptions("42", "589", "0", "201");
        Question question4 = new Question("What is the approximate value of pi?", "3.14");
        question4.addOptions("2.7", "6.28", "144", "3.14");
        questionList = new Question[]{question1, question2, question3, question4};
        setCurrentQuestion();
    }

    private void setPositions() {
        int questionTextLength = font.getWidth(currentQuestion.getQuestion());
        fontTextHeight = font.getHeight(currentQuestion.getQuestion());
        questionPosX = WIDTH / 2 - questionTextLength / 2;
        questionPosY = HEIGHT / 6;
        optionsPosX = WIDTH / 2 - questionTextLength / 2;
    }

    private void setCurrentQuestion() {
        Question question = questionList[(int) (Math.random() * questionList.length)];

        if (currentQuestion == question) {
            setCurrentQuestion();
        } else currentQuestion = question;
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
        Input input = container.getInput();

        if (input.isKeyPressed(Input.KEY_Q)) {
            container.exit();
        }
    }

    public void checkIfCorrectAnswer(int choice) {
        if (currentQuestion.correctAnswer(currentQuestion.getOption(choice))) {
            setCurrentQuestion();
        }
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        g.setFont(font);
        g.setBackground(Color.darkGray);
        g.setColor(Color.white);
        g.drawString(currentQuestion.getQuestion(), questionPosX, questionPosY);
        for (int i = 1; i <= 4; i++) {
            g.setColor(fontColors[i - 1]);
            g.drawString((char) (i + 64) + ". " + currentQuestion.getOption(i),
                    optionsPosX, questionPosY + fontTextHeight * i);
        }
    }
}
