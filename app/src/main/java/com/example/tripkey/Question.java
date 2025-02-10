package com.example.tripkey;

public class Question {
    private String questionText;
    private String option1;
    private String option2;

    public Question(String questionText, String option1, String option2) {
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
    }

    public String getQuestionText() { return questionText; }
    public String getOption1() { return option1; }
    public String getOption2() { return option2; }
}
