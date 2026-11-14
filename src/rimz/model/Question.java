package rimz.model;

import rimz.exception.InvalidInputException;

public abstract class Question implements Gradable, Storable {

    public static final int DEFAULT_TIME_LIMIT = 20;
    public static final int MIN_TIME_LIMIT = 5;
    public static final int MAX_TIME_LIMIT = 300;

    private String id;
    private String text;
    private String category = "General";
    private int timeLimit;

    protected Question(String id, String text, int timeLimit) throws InvalidInputException {
        setId(id);
        setText(text);
        setTimeLimit(timeLimit);
    }

    // ---------- getters ----------
    public String getId(){ 
        return id; 
    }

    public String getText(){ 
        return text; 
    }

    public String getCategory(){ 
        return category; 
    }

    public int getTimeLimit(){ 
        return timeLimit; 
    }

    // ---------- validated setters ----------
    public final void setId(String id) throws InvalidInputException {
        if (id == null || !id.trim().matches("[A-Za-z0-9_-]{1,20}")) {
            throw new InvalidInputException("Question id must be 1-20 letters, numbers, - or _.");
        }
        this.id = id.trim();
    }

    public final void setText(String text) throws InvalidInputException {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidInputException("Question text cannot be blank.");
        }
        this.text = text.trim();
    }

    public final void setCategory(String category) {
        this.category = (category == null || category.trim().isEmpty()) ? "General" : category.trim();
    }

    public final void setTimeLimit(int timeLimit) throws InvalidInputException {
        if (timeLimit < MIN_TIME_LIMIT || timeLimit > MAX_TIME_LIMIT) {
            throw new InvalidInputException("Time limit must be " + MIN_TIME_LIMIT
                + "-" + MAX_TIME_LIMIT + " seconds.");
        }
        this.timeLimit = timeLimit;
    }

    // ---------- abstract hooks ----------
    public abstract int getBasePoints();
    public abstract String getTypeName();
    /** Shown in the answer review on the results screen. */
    public abstract String getCorrectAnswerText();

    // ---------- shared behaviour ----------
    @Override
    public int calculatePoints(int secondsLeft) {
        int s = Math.max(0, Math.min(secondsLeft, timeLimit));
        int base = getBasePoints();
        return base + (int) Math.round(base * 0.5 * s / timeLimit);
    }

    protected static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    /** Rebuilds the right subclass from one question line (used by FileManager). */
    public static Question fromFileRecord(String line) throws InvalidInputException {
        if (line == null) {
            throw new InvalidInputException("Empty question record.");
        }
        String[] p = line.split("\\|", -1);
        switch (p[0].trim().toUpperCase()) {
            case "MC": return MultipleChoiceQuestion.fromParts(p);
            case "TF": return TrueFalseQuestion.fromParts(p);
            case "ID": return IdentificationQuestion.fromParts(p);
            default:   throw new InvalidInputException("Unknown question type: " + p[0]);
        }
    }

    /** Reads the time limit field shared by all three line formats. */
    protected static int parseTimeLimit(String field) throws InvalidInputException {
        try {
            return Integer.parseInt(field.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Time limit must be a number: " + field);
        }
    }

    @Override
    public String toString() {
        return "[" + getTypeName() + "] " + text;
    }
}