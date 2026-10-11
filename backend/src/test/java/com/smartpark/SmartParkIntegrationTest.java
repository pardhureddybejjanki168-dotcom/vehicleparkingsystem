package com.smartpark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpark.dto.request.LoginRequest;
import com.smartpark.dto.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SmartParkIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Integration: Public Registration, Login, and Role-Based Access Control")
    void testFullAuthAndRbacFlow() throws Exception {
        // 1. Register a student
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setFullName("Integration Student");
        registerReq.setStudentId("INT-STU-01");
        registerReq.setEmail("integration@student.edu");
        registerReq.setPassword("Password@123");
        registerReq.setConfirmPassword("Password@123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("integration@student.edu"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));

        // 2. Reject duplicate registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));

        // 3. Login as student
        LoginRequest studentLogin = new LoginRequest("integration@student.edu", "Password@123");
        MvcResult studentLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        String studentToken = objectMapper.readTree(studentLoginResult.getResponse().getContentAsString())
                .path("data").path("token").asText();

        // 4. Accessing protected endpoint without token -> 401 Unauthorized
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isUnauthorized());

        // 5. Student accessing permitted user profile -> 200 OK
        mockMvc.perform(get("/api/v1/users/profile")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("integration@student.edu"));

        // 6. Student attempting to access admin route -> 403 Forbidden
        mockMvc.perform(get("/api/v1/admin/stats")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        // 7. Login as seeded default Admin
        LoginRequest adminLogin = new LoginRequest("admin@smartpark.edu", "Admin@123456");
        MvcResult adminLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andReturn();

        String adminToken = objectMapper.readTree(adminLoginResult.getResponse().getContentAsString())
                .path("data").path("token").asText();

        // 8. Admin accessing admin stats -> 200 OK
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/v1/admin/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSlots").exists());
    }
}
