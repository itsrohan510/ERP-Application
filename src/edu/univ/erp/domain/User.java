package edu.univ.erp.domain;

import java.io.Serializable;


// User domain object: stores profile information only
// Passwords are stored separately in AuthStore (Auth DB)

public class User implements Serializable {
    private String username;
    private String displayName;
    private Role role;

    public User() {}

    public User(String username, String displayName, Role role) {
        this.username = username;
        this.displayName = displayName;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public Role getRole() { return role; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
}

