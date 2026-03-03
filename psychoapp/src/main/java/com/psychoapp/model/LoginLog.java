package com.psychoapp.model;

import java.time.LocalDateTime;

public class LoginLog {

    private int           id;
    private String        email;
    private String        role;
    private LocalDateTime loginTime;

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public String getEmail()              { return email; }
    public void setEmail(String email)    { this.email = email; }

    public String getRole()               { return role; }
    public void setRole(String role)      { this.role = role; }

    public LocalDateTime getLoginTime()   { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
}

