package rimz.model;

import rimz.exception.InvalidInputException;
import rimz.util.TextCodec;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Attempt implements Storable {

    private String quizId;
    private String player;     // the student's username
    private int score;
    private int correctCount;
    private int timeTaken;     // total seconds
    private final Map<String, Boolean> questionResults = new LinkedHashMap<>();

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

    public void addQuestionResult(String questionId, boolean correct) throws InvalidInputException {
        if (questionId == null || !questionId.trim().matches("[A-Za-z0-9_-]{1,20}")) {
            throw new InvalidInputException("Invalid question id in attempt result.");
        }
        questionResults.put(questionId.trim(), correct);
    }

    public Map<String, Boolean> getQuestionResults() {
        return Collections.unmodifiableMap(questionResults);
    }

    @Override
    public String toFileRecord() {
        String line = TextCodec.escape(quizId) + "|" + TextCodec.escape(player) + "|"
                + score + "|" + correctCount + "|" + timeTaken;
        if (!questionResults.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Boolean> e : questionResults.entrySet()) {
                if (sb.length() > 0) sb.append(';');
                sb.append(e.getKey()).append(':').append(e.getValue() ? '1' : '0');
            }
            line += "|" + sb;
        }
        return line;
    }

    public static Attempt fromFileRecord(String line) throws InvalidInputException {
        if (line == null) {
            throw new InvalidInputException("Empty attempt record.");
        }
        String[] p = line.split("\\|", -1);
        if (p.length != 5 && p.length != 6) {
            throw new InvalidInputException("Attempt record needs 5 or 6 fields: " + line);
        }
        try {
            Attempt a = new Attempt(TextCodec.unescape(p[0]), TextCodec.unescape(p[1]),
                    Integer.parseInt(p[2].trim()), Integer.parseInt(p[3].trim()),
                    Integer.parseInt(p[4].trim()));
            if (p.length == 6 && !p[5].trim().isEmpty()) {
                for (String entry : p[5].split(";")) {
                    String[] kv = entry.split(":", -1);
                    if (kv.length != 2 || !(kv[1].equals("0") || kv[1].equals("1"))) {
                        throw new InvalidInputException("Bad question result: " + entry);
                    }
                    a.addQuestionResult(kv[0], kv[1].equals("1"));
                }
            }
            return a;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Attempt record has a non-numeric field: " + line);
        }
    }

    @Override
    public String toString() {
        return player + " scored " + score + " on " + quizId;
    }
}