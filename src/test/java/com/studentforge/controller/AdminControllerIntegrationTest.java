package com.studentforge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class AdminControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    private String testPeriodId;
    private String testGroupId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        var periodRes = mockMvc.perform(post("/api/v1/admin/periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name", "Тестовый семестр",
                                "startDate", "2026-09-01",
                                "endDate", "2026-12-31"
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        testPeriodId = mapper.readTree(periodRes.getResponse().getContentAsString()).get("id").asText();
        var groupRes = mockMvc.perform(post("/api/v1/admin/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name", "Тестовая группа",
                                "periodId", testPeriodId
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        testGroupId = mapper.readTree(groupRes.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void createPeriod_ShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/v1/admin/periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name", "Осень 2026",
                                "startDate", "2026-09-01",
                                "endDate", "2026-12-31"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Осень 2026"));
    }

    @Test
    void getPeriods_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/admin/periods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updatePeriod_ShouldWork() throws Exception {
        mockMvc.perform(put("/api/v1/admin/periods/{id}", testPeriodId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name", "Обновленный семестр"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновленный семестр"));
    }

    @Test
    void createGroup_ShouldWork() throws Exception {
        mockMvc.perform(post("/api/v1/admin/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name", "Новая группа",
                                "periodId", testPeriodId
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новая группа"));
    }

    @Test
    void getGroups_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/admin/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getGroupById_ShouldWork() throws Exception {
        mockMvc.perform(get("/api/v1/admin/groups/{id}", testGroupId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testGroupId))
                .andExpect(jsonPath("$.name").value("Тестовая группа"));
    }

    @Test
    void updateGroup_ShouldWork() throws Exception {
        mockMvc.perform(put("/api/v1/admin/groups/{id}", testGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name", "Обновленная группа"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновленная группа"));
    }

    @Test
    void createSubject_ShouldWork() throws Exception {
        mockMvc.perform(post("/api/v1/admin/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name", "Физика",
                                "groupId", testGroupId,
                                "teacherName", "Петров П.П.",
                                "totalHours", 100,
                                "weightCoefficient", 0.5
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Физика"));
    }

    @Test
    void getSubjects_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/admin/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createAssignment_ShouldWork() throws Exception {
        var subjRes = mockMvc.perform(post("/api/v1/admin/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "name", "Программирование",
                                "groupId", testGroupId,
                                "teacherName", "Козлов К.К.",
                                "totalHours", 120,
                                "weightCoefficient", 0.5
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        String subjectId = mapper.readTree(subjRes.getResponse().getContentAsString()).get("id").asText();
        mockMvc.perform(post("/api/v1/admin/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of(
                                "title", "Лабораторная работа",
                                "subjectId", subjectId,
                                "dueDate", "2026-12-25T23:59:00",
                                "type", "HOMEWORK",
                                "weight", 0.33
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Лабораторная работа"));
    }
    @Test
    void getAssignments_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/admin/assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
    @Test
    void withoutAuth_ShouldReturnUnauthorized() throws Exception {
    }
}