# RIMZ Quiz Application

Offline Java Swing quiz game for CS108 (OOP).
Teachers create quizzes with join codes; students join, play against a timer and appear on a leaderboard.

## Team
| Member | Role | Branch |
|---|---|---|
| Reynalyn | Member 1: Model & OOP design | reynalyn/model |
| Mariane | Member 2: Logic & collections | mariane/logic |
| Zedric | Member 3: Swing UI | zedric/ui |
| Ida | Member 4: Storage, exceptions & testing | ida/storage-tests |

## Folder structure
- `src/rimz/model` : Question hierarchy, Quiz, User, Attempt
- `src/rimz/service` : AuthService, QuizService, GameEngine, ReportService
- `src/rimz/storage` : FileManager
- `src/rimz/exception` : custom exceptions
- `src/rimz/util` : shared helpers
- `src/rimz/ui` : Swing screens
- `data` : users.txt, quizzes.txt, attempts.txt
- `test` : test classes

## Team rules
1. Never push directly to `main`. Work on your own branch.
2. Open a Pull Request into `main` when a task is done; one teammate must review it.
3. Commit messages start with the task ID, e.g. `M2-03 Add QuizService`.
4. Only edit files you own. Ask the owner if you need a change.
5. `main` must always compile.

## Compile and run
javac -d out $(find src-name '*.java'
java -cp out rimz.Main
