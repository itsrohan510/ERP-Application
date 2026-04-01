package edu.univ.erp.auth.session;

import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.User;

import java.io.Serializable;

// Active user session

public class Session implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private Role role;
    private long createdAt;
    private long lastActivity;

    public Session(User user) {
        this.username = user.getUsername();
        this.role = user.getRole();
        this.createdAt = System.currentTimeMillis();
        this.lastActivity = System.currentTimeMillis();
    }

    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public long getCreatedAt() { return createdAt; }
    public long getLastActivity() { return lastActivity; }
    
    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
    }
}

