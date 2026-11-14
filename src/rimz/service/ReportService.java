package rimz.service;

import rimz.exception.InvalidInputException;
import rimz.exception.QuizNotFoundException;
import rimz.model.Attempt;
import rimz.model.Question;
import rimz.model.Quiz;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportService {

    public static class Stats {
        private final int count;
        private final double average;
        private final int highest;
        private final int lowest;

        Stats(int count, double average, int highest, int lowest) {
            this.count = count;
            this.average = average;
            this.highest = highest;
            this.lowest = lowest;
        }

        public int getCount(){ 
            return count; 
        }

        public double getAverage(){ 
            return average; 
        }

        public int getHighest(){ 
            return highest; 
        }

        public int getLowest(){ 
            return lowest; 
        }

        public boolean hasData(){ 
            return count > 0; 
        }
    }

    /** How often one question was answered correctly. */
    public static class QuestionAccuracy {
        private final String questionId;
        private final String questionText;
        private final int answered;
        private final int correct;

        QuestionAccuracy(String questionId, String questionText, int answered, int correct) {
            this.questionId = questionId;
            this.questionText = questionText;
            this.answered = answered;
            this.correct = correct;
        }

        public String getQuestionId(){ 
            return questionId; 
        }

        public String getQuestionText(){ 
            return questionText; 
        }

        public int getAnswered(){ 
            return answered; 
        }

        public int getCorrect(){ 
            return correct; 
        }
        /** 0-100. Returns 0 when nobody has answered the question yet. */
        public double getPercent(){ 
            return answered == 0 ? 0 : 100.0 * correct / answered; 
        }
    }

    /** Highest score first; ties go to the lower total time, then the player name. */
    private static final Comparator<Attempt> RANKING =
        Comparator.comparingInt(Attempt::getScore).reversed()
            .thenComparingInt(Attempt::getTimeTaken)
            .thenComparing(Attempt::getPlayer, String.CASE_INSENSITIVE_ORDER);

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<Attempt> attempts = new ArrayList<>();
    private final QuizService quizService;

    public ReportService(QuizService quizService) {
        this.quizService = quizService;
    }

    // ---------- storing attempts ----------

    /** Records a finished game (from GameEngine.finish()) or an attempt loaded from file. */
    public void addAttempt(Attempt a) throws InvalidInputException {
        if (a == null) {
            throw new InvalidInputException("Attempt cannot be null.");
        }
        attempts.add(a);
    }

    /** A copy of every attempt, for saving. */
    public List<Attempt> getAllAttempts() {
        return new ArrayList<>(attempts);
    }

    /** One student's attempts, newest first (for the student's history table). */
    public List<Attempt> getAttemptsByPlayer(String username) {
        List<Attempt> result = new ArrayList<>();
        if (username != null) {
            for (Attempt a : attempts) {
                if (a.getPlayer().equalsIgnoreCase(username.trim())) {
                    result.add(a);
                }
            }
        }
        Collections.reverse(result);
        return result;
    }

    /** Removes every attempt of a deleted quiz. Returns how many were removed. */
    public int removeAttemptsForQuiz(String quizId) {
        int before = attempts.size();
        attempts.removeIf(a -> a.getQuizId().equals(quizId));
        return before - attempts.size();
    }

    // ---------- leaderboard and rank ----------

    /** All attempts of one quiz, best first. Empty list if nobody has played. */
    public List<Attempt> leaderboard(String quizId) {
        List<Attempt> result = new ArrayList<>();
        for (Attempt a : attempts) {
            if (a.getQuizId().equals(quizId)) {
                result.add(a);
            }
        }
        result.sort(RANKING);
        return result;
    }

    /** 1 = best. Attempts with the same score and time share a rank. */
    public int rankOf(Attempt target) {
        int better = 0;
        for (Attempt a : attempts) {
            if (a != target && a.getQuizId().equals(target.getQuizId())
                    && (a.getScore() > target.getScore()
                        || (a.getScore() == target.getScore() && a.getTimeTaken() < target.getTimeTaken()))) {
                better++;
            }
        }
        return better + 1;
    }

    public Stats getStats(String quizId) {
        int count = 0;
        long total = 0;
        int highest = Integer.MIN_VALUE;
        int lowest = Integer.MAX_VALUE;
        for (Attempt a : attempts) {
            if (a.getQuizId().equals(quizId)) {
                count++;
                total += a.getScore();
                highest = Math.max(highest, a.getScore());
                lowest = Math.min(lowest, a.getScore());
            }
        }
        if (count == 0) {
            return new Stats(0, 0, 0, 0);
        }
        return new Stats(count, (double) total / count, highest, lowest);
    }

    public List<QuestionAccuracy> questionAccuracy(Quiz quiz) {
        List<QuestionAccuracy> result = new ArrayList<>();
        for (Question q : quiz.getQuestions()) {
            int answered = 0;
            int correct = 0;
            for (Attempt a : attempts) {
                if (a.getQuizId().equals(quiz.getId())) {
                    Boolean right = a.getQuestionResults().get(q.getId());
                    if (right != null) {
                        answered++;
                        if (right) {
                            correct++;
                        }
                    }
                }
            }
            result.add(new QuestionAccuracy(q.getId(), q.getText(), answered, correct));
        }
        return result;
    }

    // ---------- the text report ----------

    public String buildReport(Quiz quiz) {
        StringBuilder sb = new StringBuilder();
        sb.append("RIMZ QUIZ REPORT\n");
        sb.append("================\n");
        sb.append("Quiz:       ").append(quiz.getTitle()).append(" (").append(quiz.getId()).append(")\n");
        sb.append("Category:   ").append(quiz.getCategory()).append('\n');
        sb.append("Join code:  ").append(quiz.getJoinCode()).append('\n');
        sb.append("Owner:      ").append(quiz.getOwner()).append('\n');
        sb.append("Questions:  ").append(quiz.getQuestionCount()).append('\n');
        sb.append("Generated:  ").append(LocalDateTime.now().format(STAMP)).append("\n\n");

        Stats stats = getStats(quiz.getId());
        sb.append("SUMMARY\n-------\n");
        if (!stats.hasData()) {
            sb.append("No attempts yet.\n\n");
        } else {
            sb.append("Attempts:       ").append(stats.getCount()).append('\n');
            sb.append(String.format(Locale.ROOT, "Average score:  %.1f%n", stats.getAverage()));
            sb.append("Highest score:  ").append(stats.getHighest()).append('\n');
            sb.append("Lowest score:   ").append(stats.getLowest()).append("\n\n");

            sb.append("LEADERBOARD\n-----------\n");
            sb.append(String.format(Locale.ROOT, "%-5s %-16s %7s %8s %7s%n",
                    "Rank", "Player", "Score", "Correct", "Time"));
            for (Attempt a : leaderboard(quiz.getId())) {
                sb.append(String.format(Locale.ROOT, "%-5d %-16s %7d %8d %6ds%n",
                        rankOf(a), cut(a.getPlayer(), 16), a.getScore(), a.getCorrectCount(), a.getTimeTaken()));
            }
            sb.append('\n');
        }

        sb.append("QUESTION ACCURACY\n-----------------\n");
        boolean anyData = false;
        for (QuestionAccuracy qa : questionAccuracy(quiz)) {
            if (qa.getAnswered() > 0) {
                anyData = true;
                sb.append(String.format(Locale.ROOT, "%-6s %-40s %3d/%-3d %5.0f%%%n",
                        qa.getQuestionId(), cut(qa.getQuestionText(), 40),
                        qa.getCorrect(), qa.getAnswered(), qa.getPercent()));
            }
        }
        if (!anyData) {
            sb.append("No per-question data yet.\n");
        }
        return sb.toString();
    }

    /**
     * Writes the report to a .txt file with a BufferedWriter.
     * @throws IOException if the quiz does not exist or the file cannot be written
     *         (read-only folder, missing folder, locked file...)
     */
    public void export(String quizId, File target) throws IOException {
        if (target == null) {
            throw new IOException("No file was chosen.");
        }
        Quiz quiz;
        try {
            quiz = quizService.findById(quizId);
        } catch (QuizNotFoundException e) {
            throw new IOException("Cannot export: " + e.getMessage(), e);
        }
        String report = buildReport(quiz);
        try (BufferedWriter out = Files.newBufferedWriter(target.toPath(), StandardCharsets.UTF_8)) {
            out.write(report);
        }
    }

    private static String cut(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}