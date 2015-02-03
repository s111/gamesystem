package com.github.s111.bachelor.quizzer.game;

import com.github.s111.bachelor.quizzer.Application;
import com.github.s111.bachelor.quizzer.network.GameSession;
import org.newdawn.slick.*;
import org.newdawn.slick.Graphics;

import java.awt.Font;

public class Quizzer extends BasicGame {

    private GameSession gameSession;
    private int width;
    private int height;

    private Font awtFont;
    private TrueTypeFont font;
    private int fontTextHeight;

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

        width = container.getWidth();
        height = container.getHeight();

        initiateQuestions();
        setPositions();
    }

    private void initiateQuestions() {
        Question question1 = new Question("What is the Capital of China?", "Beijing");
        question1.addOptions("Beijing", "Washington", "Storhaug", "Hong Kong");
        Question question2 = new Question("What is the best hero in DOTKA?", "Tusk");
        question2.addOptions("Meepo", "Bear of shock ass bitch!", "Tusk", "Bloodcyka");
        Question question3 = new Question("What is the answer to life, the universe and everything?", "42");
        question3.addOptions("Cake", "42", "Black Helicopters", "Illoominadi");
        Question question4 = new Question("What is the approximate value of pi?", "3.14");
        question4.addOptions("2.7", "6.28", "144", "3.14");
        questionList = new Question[]{question1, question2, question3, question4};
        setCurrentQuestion();
    }

    private void setPositions() {
        int questionTextLength = font.getWidth(currentQuestion.getQuestion());
        fontTextHeight = font.getHeight(currentQuestion.getQuestion());
        questionPosX = width / 2 - questionTextLength / 2;
        questionPosY = height / 6;
        optionsPosX =  width / 2 - questionTextLength / 2;
    }

    private void setCurrentQuestion() {
        currentQuestion = questionList[(int) (Math.random() * questionList.length)];
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
    }

    public void checkIfCorrectAnswer(int choice) {
        if (currentQuestion.correctAnswer(currentQuestion.getOption(choice))) {
            setCurrentQuestion();
        }
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        g.setFont(font);
        g.drawString(currentQuestion.getQuestion(), questionPosX, questionPosY);
        for (int i = 1; i <= 4; i++) {
            g.drawString((char)(i + 64) + ". " + currentQuestion.getOption(i),
                    optionsPosX, questionPosY + fontTextHeight * i);
        }
    }
}
