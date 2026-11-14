package rimz.model;

import rimz.exception.InvalidInputException;
import rimz.util.TextCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultipleChoiceQuestion extends Question {

    private List<String> options = new ArrayList<>();
    private int correctIndex;

    public MultipleChoiceQuestion(String id, String text, int timeLimit, List<String> options, int correctIndex)
            throws InvalidInputException {
        super(id, text, timeLimit);
        setChoices(options, correctIndex);
    }

    public final void setChoices(List<String> newOptions, int newCorrectIndex)
            throws InvalidInputException {
        if (newOptions == null || newOptions.size() < 2 || newOptions.size() > 6) {
            throw new InvalidInputException("A multiple choice question needs 2-6 options.");
        }
        List<String> cleaned = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String o : newOptions) {
            if (o == null || o.trim().isEmpty()) {
                throw new InvalidInputException("Options cannot be blank.");
            }
            if (o.contains(";")) {
                throw new InvalidInputException("Options cannot contain ';'.");
            }
            if (!seen.add(o.trim().toLowerCase())) {
                throw new InvalidInputException("Options must be different from each other.");
            }
            cleaned.add(o.trim());
        }
        if (newCorrectIndex < 0 || newCorrectIndex >= cleaned.size()) {
            throw new InvalidInputException("Pick which option is correct.");
        }
        this.options = cleaned;
        this.correctIndex = newCorrectIndex;
    }

    public List<String> getOptions(){ 
        return Collections.unmodifiableList(options); 
    }

    public int getCorrectIndex(){ 
        return correctIndex; 
    }

    @Override 
    public int getBasePoints(){ 
        return 100; 
    }

    @Override 
    public String getTypeName(){ 
        return "Multiple Choice";
    }

    @Override 
    public String getCorrectAnswerText(){ 
        return options.get(correctIndex); 
    }

    @Override
    public boolean checkAnswer(String answer){
        return normalize(answer).equals(normalize(options.get(correctIndex)));
    }

    @Override
    public String toFileRecord() {
        StringBuilder opts = new StringBuilder();
        for (int i = 0; i < options.size(); i++) {
            if (i > 0) opts.append(';');
            opts.append(TextCodec.escape(options.get(i)));
        }
        return "MC|" + TextCodec.escape(getId()) + "|" + TextCodec.escape(getText()) + "|" + getTimeLimit() + "|" + opts + "|" + correctIndex;
    }

    static MultipleChoiceQuestion fromParts(String[] p) throws InvalidInputException {
        if (p.length != 6) {
            throw new InvalidInputException("MC record needs 6 fields.");
        }
        List<String> opts = new ArrayList<>();
        for (String o : p[4].split(";", -1)) {
            opts.add(TextCodec.unescape(o));
        }
        int idx;
        try {
            idx = Integer.parseInt(p[5].trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("MC correct index must be a number: " + p[5]);
        }
        return new MultipleChoiceQuestion(TextCodec.unescape(p[1]), TextCodec.unescape(p[2]),
                parseTimeLimit(p[3]), opts, idx);
    }
}