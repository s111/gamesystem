package com.github.s111.bachelor.quizzer.game;

import com.github.s111.bachelor.quizzer.Application;
import com.github.s111.bachelor.quizzer.network.GameSession;
import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Quizzer extends BasicGame {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    private static final int QUESTION_TIME = 10;
    private float time = 0;

    private GameSession gameSession;

    private Font awtFont;
    private TrueTypeFont font;
    private int fontTextHeight;
    private Color[] fontColors;

    private int questionPosX;
    private int questionPosY;
    private int optionsPosX;

    private ArrayList<Question> questionList;
    private Question currentQuestion;

    private GameSession.Player winner;

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
        questionList = new ArrayList<>();
        Question question1 = new Question("What is the Capital of China?", "Beijing");
        question1.addOptions("Beijing", "Washington", "Storhaug", "Hong Kong");
        Question question2 = new Question("What is the tallest mountain in Norway?", "Galdhøpiggen");
        question2.addOptions("Glittertinden", "Veslpiggen", "Galdhøpiggen", "Sentraltind");
        Question question3 = new Question("How old is the norwegian constitution?", "201");
        question3.addOptions("42", "589", "0", "201");
        Question question4 = new Question("What is the approximate value of pi?", "3.14");
        question4.addOptions("2.7", "6.28", "144", "3.14");
        questionList.add(question1);
        questionList.add(question2);
        questionList.add(question3);
        questionList.add(question4);
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
        int number = (int) (Math.random() * questionList.size());
        Question question = questionList.get(number);
        currentQuestion = question;
        questionList.remove(number);
        if (questionList.size() <= 0) {
            winner = gameSession.getWinner();
        }
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
        time += delta;
        Input input = container.getInput();

        if (winner == null && time / 1000 >= QUESTION_TIME) {
            gameSession.updateScores();
            setCurrentQuestion();
            time = 0;
        }

        if (input.isKeyPressed(Input.KEY_Q)) {
            container.exit();
        }
    }

    public boolean checkIfCorrectAnswer(int choice) {
        if (currentQuestion.correctAnswer(currentQuestion.getOption(choice))) {
            return true;
        } else return false;
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        g.setFont(font);
        g.setBackground(Color.darkGray);
        g.setColor(Color.white);

        if (winner == null) {
            g.drawString(currentQuestion.getQuestion(), questionPosX, questionPosY);
            for (int i = 1; i <= 4; i++) {
                g.setColor(fontColors[i - 1]);
                g.drawString((char) (i + 64) + ". " + currentQuestion.getOption(i),
                        optionsPosX, questionPosY + fontTextHeight * i);
            }

            drawScoreAndTime(g);
        } else {
            drawWinner(g);
        }
    }

    private void drawScoreAndTime(Graphics g) {
        List<Integer> scores = gameSession.getScores();
        g.drawString("Score: " + scores.toString(), 30, 30);
        g.drawString("Time left: " + (int) Math.ceil(QUESTION_TIME - time / 1000), WIDTH - 200, HEIGHT - 200);
    }

    private void drawWinner(Graphics g) {
        g.drawString("Winner: " + winner.getId() + " | Score: " + winner.getScore() +"!", WIDTH/2, HEIGHT/2);
    }
}
