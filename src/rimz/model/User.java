package rimz.model;

import rimz.exception.InvalidInputException;
import rimz.util.TextCodec;

/**
 * A RIMZ account (teacher or student).
 * File format (users.txt): role|username|fullName|passwordHash
 * Only the SHA-256 hash is stored, never the plain password.
 */
public class User implements Storable {

    public enum Role { TEACHER, STUDENT }

    private String username;
    private String fullName;
    private String passwordHash;
    private Role role;

    public User(String username, String fullName, String passwordHash, Role role)
            throws InvalidInputException {
        setUsername(username);
        setFullName(fullName);
        setPasswordHash(passwordHash);
        setRole(role);
    }

    // ---------- getters ----------
    public String getUsername(){ 
        return username; 
    }

    public String getFullName(){ 
        return fullName; 
    }

    public String getPasswordHash(){ 
        return passwordHash; 
    }

    public Role getRole(){ 
        return role; 
    }

    // ---------- validated setters ----------
    /** 3-20 characters: letters, digits or underscore. Stored in lowercase. */
    public void setUsername(String username) throws InvalidInputException {
        if (username == null || !username.trim().matches("[A-Za-z0-9_]{3,20}")) {
            throw new InvalidInputException(
                "Username must be 3-20 characters (letters, numbers, underscore).");
        }
        this.username = username.trim().toLowerCase();
    }

    public void setFullName(String fullName) throws InvalidInputException {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new InvalidInputException("Full name cannot be blank.");
        }
        this.fullName = fullName.trim();
    }

    public void setPasswordHash(String passwordHash) throws InvalidInputException {
        if (passwordHash == null || !passwordHash.matches("[0-9a-fA-F]{64}")) {
            throw new InvalidInputException("Invalid password hash.");
        }
        this.passwordHash = passwordHash.toLowerCase();
    }

    public void setRole(Role role) throws InvalidInputException {
        if (role == null) {
            throw new InvalidInputException("Role must be TEACHER or STUDENT.");
        }
        this.role = role;
    }

    public boolean isTeacher(){ 
        return role == Role.TEACHER; 
    }

    public boolean isStudent(){ 
        return role == Role.STUDENT; 
    }

    // ---------- Storable ----------
    @Override
    public String toFileRecord() {
        return role.name() + "|" + TextCodec.escape(username) + "|" + TextCodec.escape(fullName) + "|" + passwordHash;
    }

    /**
     * Rebuilds a User from one users.txt line (used by FileManager).
     * Throws InvalidInputException on a bad line so the caller can skip and log it.
     */
    public static User fromFileRecord(String line) throws InvalidInputException {
        if (line == null) {
            throw new InvalidInputException("Empty user record.");
        }
        String[] p = line.split("\\|", -1);
        if (p.length != 4) {
            throw new InvalidInputException("User record needs 4 fields: " + line);
        }
        Role r;
        try {
            r = Role.valueOf(p[0].trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Unknown role: " + p[0]);
        }
        return new User(TextCodec.unescape(p[1]), TextCodec.unescape(p[2]), p[3].trim(), r);
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ", " + role + ")";
    }
}