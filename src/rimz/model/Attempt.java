package rimz.model;

import rimz.exception.InvalidInputException;
import rimz.util.TextCodec;

/**
 * One finished play of a quiz by a student.
 * File format (attempts.txt): quizId|player|score|correct|totalSeconds
 */
public class Attempt implements Storable {

    private String quizId;
    private String player;     // the student's username
    private int score;
    private int correctCount;
    private int timeTaken;     // total seconds

    public Attempt(String quizId, String player, int score, int correctCount, int timeTaken)
            throws InvalidInputException {
        setQuizId(quizId);
        setPlayer(player);
        setScore(score);
        setCorrectCount(correctCount);
        setTimeTaken(timeTaken);
    }

    // ---------- getters ----------
    public String getQuizId(){ 
        return quizId; 
    }

    public String getPlayer(){
        return player; 
    }

    public int getScore(){
        return score; 
    }

    public int getCorrectCount(){ 
        return correctCount; 
    }

    public int getTimeTaken(){ 
        return timeTaken; 
    }

    // ---------- validated setters ----------
    public void setQuizId(String quizId) throws InvalidInputException {
        if (quizId == null || quizId.trim().isEmpty()) {
            throw new InvalidInputException("Quiz id cannot be blank.");
        }
        this.quizId = quizId.trim();
    }

    public void setPlayer(String player) throws InvalidInputException {
        if (player == null || player.trim().isEmpty()) {
            throw new InvalidInputException("Player cannot be blank.");
        }
        this.player = player.trim();
    }

    public void setScore(int score) throws InvalidInputException {
        if (score < 0) {
            throw new InvalidInputException("Score cannot be negative.");
        }
        this.score = score;
    }

    public void setCorrectCount(int correctCount) throws InvalidInputException {
        if (correctCount < 0) {
            throw new InvalidInputException("Correct count cannot be negative.");
        }
        this.correctCount = correctCount;
    }

    public void setTimeTaken(int timeTaken) throws InvalidInputException {
        if (timeTaken < 0) {
            throw new InvalidInputException("Time taken cannot be negative.");
        }
        this.timeTaken = timeTaken;
    }

    // ---------- Storable ----------
    @Override
    public String toFileRecord() {
        return TextCodec.escape(quizId) + "|" + TextCodec.escape(player) + "|" + score + "|" + correctCount + "|" + timeTaken;
    }

    /** Rebuilds an Attempt from one attempts.txt line (used by FileManager). */
    public static Attempt fromFileRecord(String line) throws InvalidInputException {
        if (line == null) {
            throw new InvalidInputException("Empty attempt record.");
        }
        String[] p = line.split("\\|", -1);
        if (p.length != 5) {
            throw new InvalidInputException("Attempt record needs 5 fields: " + line);
        }
        try {
            return new Attempt(TextCodec.unescape(p[0]), TextCodec.unescape(p[1]),
                    Integer.parseInt(p[2].trim()), Integer.parseInt(p[3].trim()),
                    Integer.parseInt(p[4].trim()));
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Attempt record has a non-numeric field: " + line);
        }
    }

    @Override
    public String toString() {
        return player + " scored " + score + " on " + quizId;
    }
}