package rimz.service;

import rimz.exception.InvalidInputException;
import rimz.model.Attempt;
import rimz.model.Question;
import rimz.model.Quiz;
import rimz.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameEngine {

    /** What happened on one question (for feedback and the answer review). */
    public static class AnswerRecord {
        private final String questionId;
        private final String questionText;
        private final String givenAnswer;       // null = time ran out
        private final String correctAnswer;
        private final boolean correct;
        private final int points;
        private final int secondsUsed;

        AnswerRecord(Question q, String givenAnswer, boolean correct, int points, int secondsUsed) {
            this.questionId = q.getId();
            this.questionText = q.getText();
            this.givenAnswer = givenAnswer;
            this.correctAnswer = q.getCorrectAnswerText();
            this.correct = correct;
            this.points = points;
            this.secondsUsed = secondsUsed;
        }

        public String getQuestionId()    { return questionId; }
        public String getQuestionText()  { return questionText; }
        /** The student's answer, or null if time ran out. */
        public String getGivenAnswer()   { return givenAnswer; }
        public String getCorrectAnswer() { return correctAnswer; }
        public boolean isCorrect()       { return correct; }
        public int getPoints()           { return points; }
        public int getSecondsUsed()      { return secondsUsed; }
        public boolean isTimedOut()      { return givenAnswer == null; }
    }

    private final Quiz quiz;
    private final User student;
    private final List<Question> questions;
    private final List<AnswerRecord> review = new ArrayList<>();

    private int index = 0;
    private boolean currentAnswered = false;
    private int score = 0;
    private int correctCount = 0;
    private int totalSeconds = 0;
    private Attempt attempt;                    // set by finish()

    /**
     * @throws InvalidInputException if the quiz or student is missing, the quiz has not been
     *         added to QuizService yet (no id), or the quiz has no questions
     */
    public GameEngine(Quiz quiz, User student) throws InvalidInputException {
        if (quiz == null || student == null) {
            throw new InvalidInputException("A quiz and a student are required.");
        }
        if (quiz.getId() == null) {
            throw new InvalidInputException("This quiz has not been saved yet.");
        }
        if (quiz.getQuestionCount() == 0) {
            throw new InvalidInputException("This quiz has no questions yet.");
        }
        this.quiz = quiz;
        this.student = student;
        this.questions = new ArrayList<>(quiz.getQuestions());
    }

    // ---------- current state ----------
    public Quiz getQuiz(){ 
        return quiz; 
    }

    public User getStudent(){ 
        return student; 
    }

    public Question getCurrentQuestion(){ 
        return questions.get(index); 
    }

    public int getCurrentNumber(){ 
        return index + 1; 
    }

    public int getTotalQuestions(){ 
        return questions.size(); 
    }

    public int getScore(){ 
        return score; 
    }

    public int getCorrectCount(){ 
        return correctCount; 
    }

    public boolean isCurrentAnswered(){ 
        return currentAnswered; 
    }

    public boolean isFinished(){ 
        return attempt != null; 
    }

    /** The record of the question just answered or timed out, or null if none yet. */
    public AnswerRecord getLastAnswer() {
        return review.isEmpty() ? null : review.get(review.size() - 1);
    }

    /** Every question answered so far, in order (for the results screen). */
    public List<AnswerRecord> getReview() {
        return Collections.unmodifiableList(review);
    }

    /**
     * Grades the answer for the current question.
     * @param answer      MC: the option text; TF: "true"/"false"; ID: the typed text
     * @param secondsLeft seconds still on the timer (kept between 0 and the time limit)
     * @return points earned (0 if wrong). A second answer to the same question is ignored and returns 0.
     */
    public int submitAnswer(String answer, int secondsLeft) {
        checkNotFinished();
        if (currentAnswered) {
            return 0;
        }
        Question q = getCurrentQuestion();                       // parent type: polymorphism
        int left = Math.max(0, Math.min(secondsLeft, q.getTimeLimit()));
        boolean correct = q.checkAnswer(answer);
        int points = correct ? q.calculatePoints(left) : 0;
        record(new AnswerRecord(q, answer == null ? "" : answer, correct, points,
                q.getTimeLimit() - left));
        return points;
    }

    /** The timer reached 0: counts as wrong and uses the full time. Ignored if already answered. */
    public void timeExpired() {
        checkNotFinished();
        if (currentAnswered) {
            return;
        }
        Question q = getCurrentQuestion();
        record(new AnswerRecord(q, null, false, 0, q.getTimeLimit()));
    }

    public boolean hasNext() {
        return index + 1 < questions.size();
    }

    /** Moves to the next question. The current one must be answered or timed out first. */
    public void next() {
        checkNotFinished();
        if (!currentAnswered) {
            throw new IllegalStateException("Answer the question (or let time run out) first.");
        }
        if (!hasNext()) {
            throw new IllegalStateException("There are no more questions. Call finish().");
        }
        index++;
        currentAnswered = false;
    }

    public Attempt finish() {
        if (attempt != null) {
            return attempt;
        }
        if (review.size() < questions.size()) {
            throw new IllegalStateException("Answer every question before finishing.");
        }
        try {
            Attempt a = new Attempt(quiz.getId(), student.getUsername(), score, correctCount, totalSeconds);
            for (AnswerRecord r : review) {
                a.addQuestionResult(r.getQuestionId(), r.isCorrect());
            }
            attempt = a;
            return a;
        } catch (InvalidInputException e) {
            throw new IllegalStateException("Could not build the attempt: " + e.getMessage(), e);
        }
    }

    // ---------- helpers ----------
    private void record(AnswerRecord r) {
        review.add(r);
        currentAnswered = true;
        score += r.getPoints();
        totalSeconds += r.getSecondsUsed();
        if (r.isCorrect()) {
            correctCount++;
        }
    }

    private void checkNotFinished() {
        if (attempt != null) {
            throw new IllegalStateException("This game is already finished.");
        }
    }
}