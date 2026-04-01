package edu.univ.erp.auth.store;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

// Separate storage for password hashes (Auth DB)

public class AuthStore implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FILE = "auth_store.bin";

    // Map: username -> password hash
    private Map<String, String> passwordHashes = new HashMap<>();
    // Map: username -> failed login attempts count
    private Map<String, Integer> failedAttempts = new HashMap<>();
    // Map: username -> account locked until timestamp (millis)
    private Map<String, Long> lockedUntil = new HashMap<>();

    private static AuthStore instance;

    private AuthStore() {}

    public static synchronized AuthStore getInstance() {
        if (instance == null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE))) {
                instance = (AuthStore) ois.readObject();
            } catch (Exception e) {
                instance = new AuthStore();
                instance.save();
            }
        }
        return instance;
    }

    public synchronized void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPasswordHash(String username, String hash) {
        passwordHashes.put(username, hash);
        save();
    }

    public String getPasswordHash(String username) {
        return passwordHashes.get(username);
    }

    public boolean hasUser(String username) {
        return passwordHashes.containsKey(username);
    }

    public void recordFailedAttempt(String username) {
        int attempts = failedAttempts.getOrDefault(username, 0) + 1;
        failedAttempts.put(username, attempts);
        if (attempts >= 5) {
            // Lock account for 15 minutes
            lockedUntil.put(username, System.currentTimeMillis() + (15 * 60 * 1000));
        }
        save();
    }

    public void clearFailedAttempts(String username) {
        failedAttempts.remove(username);
        lockedUntil.remove(username);
        save();
    }

    public boolean isLocked(String username) {
        Long lockTime = lockedUntil.get(username);
        if (lockTime == null) return false;
        if (System.currentTimeMillis() > lockTime) {
            // Lock expired
            lockedUntil.remove(username);
            failedAttempts.remove(username);
            save();
            return false;
        }
        return true;
    }

    public long getLockTimeRemaining(String username) {
        Long lockTime = lockedUntil.get(username);
        if (lockTime == null) return 0;
        long remaining = lockTime - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    public int getFailedAttempts(String username) {
        return failedAttempts.getOrDefault(username, 0);
    }
}

