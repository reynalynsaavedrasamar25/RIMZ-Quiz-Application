package rimz.model;

public interface Gradable {
    boolean checkAnswer(String answer);
    int calculatePoints(int secondsLeft);
}