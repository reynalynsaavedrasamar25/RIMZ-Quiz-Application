package rimz.model;

import rimz.exception.InvalidInputException;
import rimz.util.TextCodec;

public class TrueFalseQuestion extends Question {

    private boolean correctAnswer;

    public TrueFalseQuestion(String id, String text, int timeLimit, boolean correctAnswer)
            throws InvalidInputException {
        super(id, text, timeLimit);
        this.correctAnswer = correctAnswer;
    }

    public boolean getCorrectAnswer(){ 
        return correctAnswer; 
    }

    public void setCorrectAnswer(boolean answer){ 
        this.correctAnswer = answer; 
    }

    @Override 
    public int getBasePoints(){ 
        return 80; 
    }

    @Override 
    public String getTypeName(){ 
        return "True/False"; 
    }

    @Override 
    public String getCorrectAnswerText(){ 
        return correctAnswer ? "True" : "False"; 
    }

    @Override
    public boolean checkAnswer(String answer) {
        return normalize(answer).equals(String.valueOf(correctAnswer));
    }

    @Override
    public String toFileRecord() {
        return "TF|" + TextCodec.escape(getId()) + "|" + TextCodec.escape(getText()) + "|" + getTimeLimit() + "|" + correctAnswer;
    }

    static TrueFalseQuestion fromParts(String[] p) throws InvalidInputException {
        if (p.length != 5) {
            throw new InvalidInputException("TF record needs 5 fields.");
        }
        String v = p[4].trim().toLowerCase();
        if (!v.equals("true") && !v.equals("false")) {
            throw new InvalidInputException("TF answer must be true or false: " + p[4]);
        }
        return new TrueFalseQuestion(TextCodec.unescape(p[1]), TextCodec.unescape(p[2]),
                parseTimeLimit(p[3]), v.equals("true"));
    }
}