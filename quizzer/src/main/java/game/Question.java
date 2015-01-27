package game;

/**
 * Created by Jonas on 27.01.2015.
 */
public class Question {
    private String question;
    private String answer;
    private String[] options;

    public Question(String q, String a, String[] o) {
        this.question = q;
        this.answer = a;
        this.options = o;
    }

    public boolean correctAnswer(String answer, String choice) {
        if (answer.equals(choice)) {
        return true;
        } else return false;
    }
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getOption(int index) {
        return options[index - 1];
    }

    public void setOptions(String[] options) {
        this.options = options;
    }
}
