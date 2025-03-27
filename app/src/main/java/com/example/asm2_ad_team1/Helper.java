package com.example.asm2_ad_team1;

public class Helper {
    String username, email, password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Helper(String username , String email, String password) {
        this.email = email;
        this.password = password;
        this.username = username;

    }
}
