package com.studentforge.controller;

import com.studentforge.entity.*;
import com.studentforge.enums.AssignmentStatus;
import com.studentforge.enums.AssignmentType;
import com.studentforge.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(roles = "USER")
class SubjectControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AcademicPeriodRepository periodRepository;

    private MockMvc mockMvc;
    private UUID testGroupId;
    private UUID testSubjectId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        assignmentRepository.deleteAll();
        subjectRepository.deleteAll();
        groupRepository.deleteAll();
        periodRepository.deleteAll();
        AcademicPeriod period = new AcademicPeriod();
        period.setName("Тестовый семестр");
        period.setStartDate(java.time.LocalDate.of(2026, 9, 1));
        period.setEndDate(java.time.LocalDate.of(2026, 12, 31));
        period.setActive(true);
        period = periodRepository.save(period);
        Group group = new Group();
        group.setName("Тестовая группа");
        group.setPeriod(period);
        group = groupRepository.save(group);
        testGroupId = group.getId();
        Subject subject = new Subject();
        subject.setName("Математика");
        subject.setTeacherName("Иванов И.И.");
        subject.setTotalHours(100);
        subject.setGroup(group);
        subject.setWeightCoefficient(new java.math.BigDecimal("0.5"));
        subject = subjectRepository.save(subject);
        testSubjectId = subject.getId();
        Assignment assignment = new Assignment();
        assignment.setTitle("Домашняя работа");
        assignment.setSubject(subject);
        assignment.setDueDate(LocalDateTime.now().plusDays(7));
        assignment.setStatus(AssignmentStatus.PENDING);
        assignment.setType(AssignmentType.HOMEWORK);
        assignment.setWeight(new java.math.BigDecimal("0.33"));
        assignmentRepository.save(assignment);
    }

    @Test
    void getGroupSubjects_ShouldReturnSubjects() throws Exception {
        mockMvc.perform(get("/api/v1/groups/" + testGroupId + "/subjects")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getSubjectAssignments_ShouldReturnAssignments() throws Exception {
        mockMvc.perform(get("/api/v1/subjects/" + testSubjectId + "/assignments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Домашняя работа"));
    }
}