package rimz.model;

import rimz.exception.InvalidInputException;
import rimz.util.TextCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Quiz implements Storable {

    private String id;          // null until added to QuizService
    private String title;
    private String category;
    private String owner;       // teacher's username
    private String joinCode;    // null until added to QuizService
    private final ArrayList<Question> questions = new ArrayList<>();

    /** For a brand-new quiz made in the editor. */
    public Quiz(String title, String category, String owner) throws InvalidInputException {
        setTitle(title);
        setCategory(category);
        setOwner(owner);
    }

    /** For a quiz loaded from file (id and code already exist). */
    public Quiz(String id, String title, String category, String owner, String joinCode)
            throws InvalidInputException {
        this(title, category, owner);
        setId(id);
        setJoinCode(joinCode);
    }

    // ---------- getters ----------
    public String getId(){ 
        return id; 
    }

    public String getTitle(){ 
        return title; 
    }

    public String getCategory(){ 
        return category; 
    }

    public String getOwner(){ 
        return owner; 
    }

    public String getJoinCode(){ 
        return joinCode; 
    }

    public int getQuestionCount(){ 
        return questions.size(); 
    }

    /** Read-only view. Change questions with addQuestion/removeQuestion/updateQuestion. */
    public List<Question> getQuestions(){
        return Collections.unmodifiableList(questions); 
    }

    // ---------- validated setters ----------
    public final void setId(String id) throws InvalidInputException {
        if (id == null || !id.trim().matches("[A-Za-z0-9_-]{1,20}")) {
            throw new InvalidInputException("Quiz id must be 1-20 letters, numbers, - or _.");
        }
        this.id = id.trim();
    }

    public final void setTitle(String title) throws InvalidInputException {
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidInputException("Quiz title cannot be blank.");
        }
        this.title = title.trim();
    }

    public final void setCategory(String category) {
        this.category = (category == null || category.trim().isEmpty()) ? "General" : category.trim();
    }

    public final void setOwner(String owner) throws InvalidInputException {
        if (owner == null || owner.trim().isEmpty()) {
            throw new InvalidInputException("Quiz owner cannot be blank.");
        }
        this.owner = owner.trim();
    }

    public final void setJoinCode(String code) throws InvalidInputException {
        if (code == null || !code.trim().toUpperCase().matches("[A-Z0-9]{6}")) {
            throw new InvalidInputException("Join code must be 6 letters or numbers.");
        }
        this.joinCode = code.trim().toUpperCase();
    }

    // ---------- question management ----------
    public void addQuestion(Question q) throws InvalidInputException {
        if (q == null) {
            throw new InvalidInputException("Question cannot be null.");
        }
        if (indexOf(q.getId()) >= 0) {
            throw new InvalidInputException("Duplicate question id: " + q.getId());
        }
        questions.add(q);
    }

    public void removeQuestion(String questionId) throws InvalidInputException {
        int i = indexOf(questionId);
        if (i < 0) {
            throw new InvalidInputException("No question with id: " + questionId);
        }
        questions.remove(i);
    }

    /** Replaces the question that has the same id. */
    public void updateQuestion(Question q) throws InvalidInputException {
        if (q == null) {
            throw new InvalidInputException("Question cannot be null.");
        }
        int i = indexOf(q.getId());
        if (i < 0) {
            throw new InvalidInputException("No question with id: " + q.getId());
        }
        questions.set(i, q);
    }

    public Question getQuestion(String questionId) {
        int i = indexOf(questionId);
        return i < 0 ? null : questions.get(i);
    }

    private int indexOf(String questionId) {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getId().equals(questionId)) {
                return i;
            }
        }
        return -1;
    }

    // ---------- Storable ----------
    @Override
    public String toFileRecord() {
        return "QUIZ|" + TextCodec.escape(id == null ? "" : id) + "|" + TextCodec.escape(title) + "|" + TextCodec.escape(category) + "|" + TextCodec.escape(owner) + "|" + (joinCode == null ? "" : joinCode);
    }

    public List<String> toFileLines() {
        List<String> lines = new ArrayList<>();
        lines.add(toFileRecord());
        for (Question q : questions) {
            lines.add(q.toFileRecord());
        }
        return lines;
    }

    public static Quiz fromFileRecord(String line) throws InvalidInputException {
        if (line == null) {
            throw new InvalidInputException("Empty quiz record.");
        }
        String[] p = line.split("\\|", -1);
        if (p.length != 6 || !p[0].trim().equalsIgnoreCase("QUIZ")) {
            throw new InvalidInputException("Quiz record needs 6 fields starting with QUIZ: " + line);
        }
        return new Quiz(TextCodec.unescape(p[1]), TextCodec.unescape(p[2]),
                TextCodec.unescape(p[3]), TextCodec.unescape(p[4]), p[5]);
    }

    @Override
    public String toString() {
        return title + " [" + joinCode + "] (" + questions.size() + " questions)";
    }
}