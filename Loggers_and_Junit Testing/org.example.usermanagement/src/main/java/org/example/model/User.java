package org.example.model;

public class User {

    private int userId;
    private String name;
    private String email;
    private int age;
    private String phone;
    private String password;
    private String status;

    public User(){}

    public User(String name, String email, int age, String phone, String password, String status) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.phone = phone;
        this.password = password;
        this.status = status;
    }

    // Getters & Setters Methods
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
