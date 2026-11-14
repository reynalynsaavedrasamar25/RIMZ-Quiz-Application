package rimz.service;

import rimz.exception.InvalidInputException;
import rimz.exception.QuizNotFoundException;
import rimz.model.Quiz;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class QuizService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    private final Map<String, Quiz> quizzes = new HashMap<>();
    private final Set<String> joinCodes = new HashSet<>();
    private final Random random = new Random();

    /** Adds a new quiz. Assigns an id and join code if the quiz has none yet. */
    public void add(Quiz q) throws InvalidInputException {
        if (q == null) {
            throw new InvalidInputException("Quiz cannot be null.");
        }
        if (q.getId() != null && quizzes.containsKey(q.getId())) {
            throw new InvalidInputException("A quiz with id " + q.getId() + " already exists.");
        }
        if (q.getJoinCode() != null && joinCodes.contains(q.getJoinCode())) {
            throw new InvalidInputException("Join code " + q.getJoinCode() + " is already used.");
        }
        if (q.getId() == null) {
            q.setId(nextId());
        }
        if (q.getJoinCode() == null) {
            q.setJoinCode(generateJoinCode());
        }
        quizzes.put(q.getId(), q);
        joinCodes.add(q.getJoinCode());
    }

    /** Replaces the stored quiz that has the same id. The join code never changes. */
    public void update(Quiz q) throws InvalidInputException, QuizNotFoundException {
        if (q == null || q.getId() == null) {
            throw new InvalidInputException("Quiz must already have an id to be updated.");
        }
        Quiz old = quizzes.get(q.getId());
        if (old == null) {
            throw new QuizNotFoundException("No quiz with id " + q.getId());
        }
        q.setJoinCode(old.getJoinCode());
        quizzes.put(q.getId(), q);
    }

    public void delete(String id) throws QuizNotFoundException {
        Quiz removed = id == null ? null : quizzes.remove(id.trim());
        if (removed == null) {
            throw new QuizNotFoundException("No quiz with id " + id);
        }
        joinCodes.remove(removed.getJoinCode());
    }

    public Quiz findById(String id) throws QuizNotFoundException {
        Quiz q = id == null ? null : quizzes.get(id.trim());
        if (q == null) {
            throw new QuizNotFoundException("No quiz with id " + id);
        }
        return q;
    }

    /** Case-insensitive; surrounding spaces are ignored. */
    public Quiz findByCode(String code) throws QuizNotFoundException {
        if (code != null) {
            String wanted = code.trim().toUpperCase();
            for (Quiz q : quizzes.values()) {
                if (q.getJoinCode().equals(wanted)) {
                    return q;
                }
            }
        }
        throw new QuizNotFoundException("No quiz with join code " + code);
    }

    /** All quizzes, A-Z by title. */
    public List<Quiz> getAll() {
        return sortByTitle(new ArrayList<>(quizzes.values()));
    }

    /** One teacher's quizzes. */
    public List<Quiz> getByOwner(String username) {
        List<Quiz> result = new ArrayList<>();
        if (username != null) {
            for (Quiz q : quizzes.values()) {
                if (q.getOwner().equalsIgnoreCase(username.trim())) {
                    result.add(q);
                }
            }
        }
        return sortByTitle(result);
    }

    /** Keyword matches the title or category. Blank keyword returns everything. */
    public List<Quiz> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Quiz> result = new ArrayList<>();
        for (Quiz q : quizzes.values()) {
            if (k.isEmpty() || q.getTitle().toLowerCase().contains(k)
                    || q.getCategory().toLowerCase().contains(k)) {
                result.add(q);
            }
        }
        return sortByTitle(result);
    }

    /** Exact category (ignoring case). Blank or "All" returns everything. */
    public List<Quiz> filter(String category) {
        boolean all = category == null || category.trim().isEmpty()
                || category.trim().equalsIgnoreCase("All");
        List<Quiz> result = new ArrayList<>();
        for (Quiz q : quizzes.values()) {
            if (all || q.getCategory().equalsIgnoreCase(category.trim())) {
                result.add(q);
            }
        }
        return sortByTitle(result);
    }

    /** Distinct categories A-Z, for the filter dropdown. */
    public List<String> getCategories() {
        Set<String> sorted = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Quiz q : quizzes.values()) {
            sorted.add(q.getCategory());
        }
        return new ArrayList<>(sorted);
    }

    /** New list, A-Z by title. Works on search/filter results so they can be combined. */
    public List<Quiz> sortByTitle(List<Quiz> list) {
        List<Quiz> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparing(Quiz::getTitle, String.CASE_INSENSITIVE_ORDER));
        return copy;
    }

    /** New list, most questions first (ties A-Z by title). */
    public List<Quiz> sortByQuestionCount(List<Quiz> list) {
        List<Quiz> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparingInt(Quiz::getQuestionCount).reversed()
                .thenComparing(Quiz::getTitle, String.CASE_INSENSITIVE_ORDER));
        return copy;
    }

    public int count() {
        return quizzes.size();
    }

    /** Registers a quiz read from quizzes.txt (it already has an id and join code). */
    public void loadQuiz(Quiz q) throws InvalidInputException {
        if (q == null || q.getId() == null || q.getJoinCode() == null) {
            throw new InvalidInputException("Loaded quiz needs an id and a join code.");
        }
        add(q);
    }

    public String nextId() {
        int max = 0;
        for (String id : quizzes.keySet()) {
            if (id.matches("Q\\d+")) {
                max = Math.max(max, Integer.parseInt(id.substring(1)));
            }
        }
        return String.format("Q%03d", max + 1);
    }

    /**
     * Makes a random 6-character code that is not used yet.
     * If the code already exists, the method calls itself again (recursion).
     */
    public String generateJoinCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        String code = sb.toString();
        if (joinCodes.contains(code)) {
            return generateJoinCode();
        }
        return code;
    }
}