package rimz.model;

import rimz.exception.InvalidInputException;
import rimz.util.TextCodec;

public class IdentificationQuestion extends Question {

    private String correctAnswer;

    public IdentificationQuestion(String id, String text, int timeLimit, String correctAnswer)
            throws InvalidInputException {
        super(id, text, timeLimit);
        setCorrectAnswer(correctAnswer);
    }

    public final void setCorrectAnswer(String answer) throws InvalidInputException {
        if (answer == null || answer.trim().isEmpty()) {
            throw new InvalidInputException("An identification question needs an answer.");
        }
        this.correctAnswer = answer.trim();
    }

    public String getCorrectAnswer() { return correctAnswer; }

    @Override 
    public int getBasePoints(){ 
        return 150; 
    }

    @Override 
    public String getTypeName(){ 
        return "Identification"; 
    }

    @Override public String getCorrectAnswerText(){ 
        return correctAnswer; 
    }

    @Override
    public boolean checkAnswer(String answer) {
        return normalize(answer).equals(normalize(correctAnswer));
    }

    @Override
    public String toFileRecord() {
        return "ID|" + TextCodec.escape(getId()) + "|" + TextCodec.escape(getText()) + "|" + getTimeLimit() + "|" + TextCodec.escape(correctAnswer);
    }

    static IdentificationQuestion fromParts(String[] p) throws InvalidInputException {
        if (p.length != 5) {
            throw new InvalidInputException("ID record needs 5 fields.");
        }
        return new IdentificationQuestion(TextCodec.unescape(p[1]), TextCodec.unescape(p[2]),
                parseTimeLimit(p[3]), TextCodec.unescape(p[4]));
    }
}