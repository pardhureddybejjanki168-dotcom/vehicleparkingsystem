package com.smartpark.dto.response;

import com.smartpark.entity.Role;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String fullName;
    private String studentId;
    private Role role;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long id, String email, String fullName, String studentId, Role role) {
        this.token = token;
        this.type = "Bearer";
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.studentId = studentId;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
