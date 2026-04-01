package edu.univ.erp.auth;

import edu.univ.erp.auth.hash.PasswordHash;
import edu.univ.erp.auth.session.Session;
import edu.univ.erp.auth.session.SessionManager;
import edu.univ.erp.auth.store.AuthStore;
import edu.univ.erp.data.DataStore;
import edu.univ.erp.domain.User;

public class AuthService {
    private final DataStore ds = DataStore.getInstance();
    private final AuthStore authStore = AuthStore.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();

    // Login flow: lookup username in Auth DB → verify typed password against hash → 
    // if OK, remember user id+role in session → load profile from ERP DB.

    public User login(String username, String password) {
        if (username == null || password == null) return null;
        
        // Checking if account is locked
        if (authStore.isLocked(username)) {
            long remaining = authStore.getLockTimeRemaining(username);
            int minutes = (int) (remaining / (60 * 1000));
            throw new SecurityException("Account locked. Try again in " + minutes + " minute(s).");
        }
        
        // Get password hash from Auth DB
        String storedHash = authStore.getPasswordHash(username);
        if (storedHash == null) {
            // User does not exist in Auth DB
            return null;
        }
        
        // Verifying password against hash
        if (!PasswordHash.verifyPassword(password, storedHash)) {
            authStore.recordFailedAttempt(username);
            return null;
        }
        
        // Correct password, clear failed attempts
        authStore.clearFailedAttempts(username);
        
        // Load user profile from ERP DB
        User user = ds.getUsers().get(username);
        if (user == null) {
            return null;
        }
        
        // Create session
        sessionManager.createSession(user);
        
        return user;
    }

    public boolean addUser(User user, String password) {
        if (ds.getUsers().containsKey(user.getUsername())) return false;
        if (authStore.hasUser(user.getUsername())) return false;
        
        // Store user profile in ERP DB (no password)
        ds.getUsers().put(user.getUsername(), user);
        ds.save();
        
        // Store password hash in Auth DB
        String hash = PasswordHash.hashPassword(password);
        authStore.setPasswordHash(user.getUsername(), hash);
        
        return true;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (username == null || oldPassword == null || newPassword == null) return false;
        if (newPassword.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters long");
        }
        
        // Verify old password
        String storedHash = authStore.getPasswordHash(username);
        if (storedHash == null || !PasswordHash.verifyPassword(oldPassword, storedHash)) {
            return false;
        }
        
        // Set new password hash
        String newHash = PasswordHash.hashPassword(newPassword);
        authStore.setPasswordHash(username, newHash);
        return true;
    }

    public Session getSession(String username) {
        return sessionManager.getSession(username);
    }

    public void logout(String username) {
        sessionManager.invalidateSession(username);
    }
}

