package org.example.models;

public class User {

    private int user_id;
    private String username;
    private int age;
    private String password;

    public User(){}

    public User(int user_id, String username, int age, String password) {
        this.user_id = user_id;
        this.username = username;
        this.age = age;
        this.password = password;
    }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}