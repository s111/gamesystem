package game;

import org.newdawn.slick.*;

/**
 * Created by Jonas on 26.01.2015.
 */
public class Quizzer extends BasicGame {
    private static final int NUM_QUESTIONS = 3;
    private Question question1;
    private String[] options1;
    private Question question2;
    private String[] options2;
    private Question question3;
    private String[] options3;
    private Question[] questionList;
    private Question currentQuestion;
    private String choice;

    public Quizzer(String title) {
        super(title);
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        initiateQuestions();
    }

    private void initiateQuestions() {
        options1 = new String[]{"Washington", "Beijing", "Storhaug", "Hong Kong"};
        question1 = new Question("What is Capital of China?", "Beijing", options1);
        options2 = new String[]{"Bloodcyka", "Bear of shock ass bitch!", "Tusk", "Meepo"};
        question2 = new Question("What is best hero in DOTKA?", "Tusk", options2);
        options3 = new String[]{"42", "Cake", "Black Helicopters", "Illoominadi"};
        question3 = new Question("What is answer to life, the universe and everything?", "42", options3);
        questionList = new Question[]{question1, question2, question3};
        setCurrentQuestion();
    }

    private void setCurrentQuestion() {
        currentQuestion = questionList[(int)(Math.random() * NUM_QUESTIONS)];
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
        Input input = container.getInput();
        answerQuestion(input);
    }

    public void answerQuestion(Input input) {
        if (input.isKeyPressed(Input.KEY_1)) {
            choice = currentQuestion.getOption(1);
            checkIfCorrectAnswer(choice);
        } else if (input.isKeyPressed(Input.KEY_2)) {
            choice = currentQuestion.getOption(2);
            checkIfCorrectAnswer(choice);
        } else if (input.isKeyPressed(Input.KEY_3)) {
            choice = currentQuestion.getOption(3);
            checkIfCorrectAnswer(choice);
        } else if (input.isKeyPressed(Input.KEY_4)) {
            choice = currentQuestion.getOption(4);
            checkIfCorrectAnswer(choice);
        }
    }

    public void checkIfCorrectAnswer(String choice) {
        if(currentQuestion.correctAnswer(currentQuestion.getAnswer(), choice)) {
            setCurrentQuestion();
        }
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        g.drawString(currentQuestion.getQuestion(), 100, 100);
        g.drawString("A. " + currentQuestion.getOption(1), 100, 120);
        g.drawString("B. " + currentQuestion.getOption(2), 100, 140);
        g.drawString("C. " + currentQuestion.getOption(3), 100, 160);
        g.drawString("D. " + currentQuestion.getOption(4), 100, 180);
    }
}
