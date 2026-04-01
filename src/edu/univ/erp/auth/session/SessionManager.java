package edu.univ.erp.auth.session;

import edu.univ.erp.domain.User;

import java.util.HashMap;
import java.util.Map;

// Manages active user sessions

public class SessionManager {
    private static final SessionManager instance = new SessionManager();
    private final Map<String, Session> activeSessions = new HashMap<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        return instance;
    }

    public Session createSession(User user) {
        Session session = new Session(user);
        activeSessions.put(user.getUsername(), session);
        return session;
    }

    public Session getSession(String username) {
        return activeSessions.get(username);
    }

    public void invalidateSession(String username) {
        activeSessions.remove(username);
    }

    public void updateActivity(String username) {
        Session session = activeSessions.get(username);
        if (session != null) {
            session.updateActivity();
        }
    }
}

