package rimz.service;

import rimz.exception.AuthenticationException;
import rimz.exception.InvalidInputException;
import rimz.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registration and login. Users live in a HashMap keyed by (lowercase) username.
 * No file or UI code here: FileManager fills it with loadUser(), then saves
 * getAllUsers() after changes.
 */
public class AuthService {

    private static final int MIN_PASSWORD_LENGTH = 4;

    private final Map<String, User> users = new HashMap<>();

    /**
     * Creates a new account.
     * @param role "TEACHER" or "STUDENT" (any letter case)
     * @throws InvalidInputException blank/invalid fields, short password, bad role, duplicate username
     */
    public void register(String name, String user, String pass, String role)
            throws InvalidInputException {
        if (name == null || name.trim().isEmpty()
                || user == null || user.trim().isEmpty()
                || pass == null || pass.isEmpty()
                || role == null || role.trim().isEmpty()) {
            throw new InvalidInputException("Please fill in all fields.");
        }
        if (pass.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidInputException(
                "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }

        User.Role parsedRole;
        try {
            parsedRole = User.Role.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Role must be TEACHER or STUDENT.");
        }

        // The User constructor validates username/name and lowercases the username.
        User newUser = new User(user, name, hashPassword(pass), parsedRole);

        if (users.containsKey(newUser.getUsername())) {
            throw new InvalidInputException("Username already taken.");
        }
        users.put(newUser.getUsername(), newUser);
    }

    /**
     * Checks credentials and returns the matching user.
     * Same message for unknown user and wrong password, so it does not reveal which one failed.
     */
    public User login(String user, String pass) throws AuthenticationException {
        if (user == null || user.trim().isEmpty() || pass == null || pass.isEmpty()) {
            throw new AuthenticationException("Enter your username and password.");
        }
        User found = users.get(user.trim().toLowerCase());
        if (found == null || !hashMatches(pass, found.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password.");
        }
        return found;
    }

    // ---------- for FileManager ----------

    /** Adds an already-built user (e.g. read from users.txt). Duplicates are rejected. */
    public void loadUser(User u) throws InvalidInputException {
        if (u == null) {
            throw new InvalidInputException("User cannot be null.");
        }
        if (users.containsKey(u.getUsername())) {
            throw new InvalidInputException("Duplicate username in data: " + u.getUsername());
        }
        users.put(u.getUsername(), u);
    }

    /** A copy of all users, for saving. */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public boolean usernameExists(String username) {
        return username != null && users.containsKey(username.trim().toLowerCase());
    }

    public int userCount() {
        return users.size();
    }

    // ---------- hashing ----------

    /** SHA-256 of the password as a 64-character lowercase hex string. */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed on every Java platform
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private boolean hashMatches(String plainPassword, String storedHash) {
        return MessageDigest.isEqual(
            hashPassword(plainPassword).getBytes(StandardCharsets.UTF_8),
            storedHash.getBytes(StandardCharsets.UTF_8));
    }
}