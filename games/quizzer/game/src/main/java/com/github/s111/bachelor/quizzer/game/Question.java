package com.github.s111.bachelor.quizzer.game;

import java.util.ArrayList;

public class Question {
    private String question;
    private String answer;
    private ArrayList<String> options;

    public Question(String question, String answer) {
        this.question = question;
        this.answer = answer;
        options = new ArrayList<String>();
    }

    public boolean correctAnswer(String choice) {
        return answer.equals(choice);
    }

    public void addOptions(String option1, String option2, String option3, String option4) {
        options.add(option1);
        options.add(option2);
        options.add(option3);
        options.add(option4);
    }

    public String getQuestion() {
        return question;
    }

    public String getOption(int index) {
        return options.get(index - 1);
    }
}
