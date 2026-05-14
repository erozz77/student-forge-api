package com.studentforge.controller;

import com.studentforge.entity.*;
import com.studentforge.enums.AssignmentStatus;
import com.studentforge.enums.AssignmentType;
import com.studentforge.enums.Role;
import com.studentforge.enums.SubmissionStatus;
import com.studentforge.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerUnitTest {

    @Mock private UserRepository userRepository;
    @Mock private AcademicPeriodRepository periodRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private SubmissionRepository submissionRepository;

    @InjectMocks
    private AdminController adminController;

    @Test
    void getPeriods_ShouldReturnList() {
        when(periodRepository.findAll()).thenReturn(List.of());
        var result = adminController.periods();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getGroups_ShouldReturnList() {
        when(groupRepository.findAll()).thenReturn(List.of());
        var result = adminController.groups();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getSubjects_ShouldReturnList() {
        when(subjectRepository.findAll()).thenReturn(List.of());
        var result = adminController.subjects();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAssignments_ShouldReturnList() {
        when(assignmentRepository.findAll()).thenReturn(List.of());
        var result = adminController.assignments();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getStudents_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of());
        var result = adminController.students();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getSubmissions_ShouldReturnList() {
        when(submissionRepository.findAll()).thenReturn(List.of());
        var result = adminController.submissions();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getGroupStudents_ShouldReturnList() {
        UUID id = UUID.randomUUID();
        when(enrollmentRepository.findAllByGroupId(id)).thenReturn(List.of());
        var result = adminController.groupStudents(id);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getUserGroups_ShouldReturnList() {
        UUID userId = UUID.randomUUID();
        when(enrollmentRepository.findAllByUserId(userId)).thenReturn(List.of());
        var result = adminController.getUserGroups(userId);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getStudentStats_ShouldReturnStats() {
        UUID userId = UUID.randomUUID();
        when(gradeRepository.findAllByUserId(userId)).thenReturn(List.of());
        var result = adminController.stats(userId);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteUser_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setRole(Role.USER);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);
        var result = adminController.deleteUser(id);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteGroup_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        Group group = new Group();
        group.setEnrollments(new ArrayList<>());
        group.setSubjects(new ArrayList<>());
        when(groupRepository.findById(id)).thenReturn(Optional.of(group));
        doNothing().when(groupRepository).delete(group);
        var result = adminController.deleteGroup(id);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteSubject_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        Subject subject = new Subject();
        subject.setAssignments(new ArrayList<>());
        when(subjectRepository.findById(id)).thenReturn(Optional.of(subject));
        doNothing().when(subjectRepository).delete(subject);
        var result = adminController.deleteSubject(id);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteAssignment_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        Assignment assignment = new Assignment();
        assignment.setGrades(new ArrayList<>());
        when(assignmentRepository.findById(id)).thenReturn(Optional.of(assignment));
        doNothing().when(assignmentRepository).delete(assignment);
        var result = adminController.deleteAssignment(id);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void enrollStudent_ShouldReturnOk() {
        Map<String, String> body = new HashMap<>();
        body.put("userId", UUID.randomUUID().toString());
        body.put("groupId", UUID.randomUUID().toString());
        Group group = new Group();
        group.setId(UUID.randomUUID());
        User user = new User();
        user.setId(UUID.randomUUID());
        when(groupRepository.findById(any(UUID.class))).thenReturn(Optional.of(group));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(new Enrollment());
        var result = adminController.enroll(body);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void unenrollStudent_ShouldReturnOk() {
        Map<String, String> body = new HashMap<>();
        body.put("userId", UUID.randomUUID().toString());
        body.put("groupId", UUID.randomUUID().toString());
        when(enrollmentRepository.findAllByUserId(any(UUID.class))).thenReturn(List.of());
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(new User()));
        var result = adminController.unenroll(body);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateUserGroupName_ShouldReturnOk() {
        UUID userId = UUID.randomUUID();
        Map<String, String> body = new HashMap<>();
        body.put("groupName", "Новая группа");
        User user = new User();
        user.setId(userId);
        user.setStudentProfile(new StudentProfile());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        var result = adminController.updateUserGroupName(userId, body);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }




    @Test
    void updatePeriod_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Обновленный семестр");
        body.put("startDate", "2026-09-01");
        body.put("endDate", "2026-12-31");
        AcademicPeriod period = new AcademicPeriod();
        period.setId(id);
        when(periodRepository.findById(id)).thenReturn(Optional.of(period));
        when(periodRepository.save(any())).thenReturn(period);
        var result = adminController.updatePeriod(id, body);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateGroup_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Обновленная группа");
        Group group = new Group();
        group.setId(id);
        AcademicPeriod period = new AcademicPeriod();
        period.setId(UUID.randomUUID());
        group.setPeriod(period);
        when(groupRepository.findById(id)).thenReturn(Optional.of(group));
        when(groupRepository.save(any())).thenReturn(group);
        var result = adminController.updateGroup(id, body);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateSubject_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Обновленный предмет");
        Subject subject = new Subject();
        subject.setId(id);
        when(subjectRepository.findById(id)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(any())).thenReturn(subject);
        var result = adminController.updateSubject(id, body);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateAssignment_ShouldReturnOk() {
        UUID id = UUID.randomUUID();
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Обновленное задание");
        Assignment assignment = new Assignment();
        assignment.setId(id);
        when(assignmentRepository.findById(id)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any())).thenReturn(assignment);
        var result = adminController.updateAssignment(id, body);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteAdminUser_ShouldReturnBadRequest() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setRole(Role.ADMIN);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        var result = adminController.deleteUser(id);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createPeriod_ShouldReturnCreated() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Новый семестр");
        body.put("startDate", "2026-09-01");
        body.put("endDate", "2026-12-31");
        AcademicPeriod period = new AcademicPeriod();
        period.setId(UUID.randomUUID());
        period.setName("Новый семестр");
        period.setStartDate(LocalDate.parse("2026-09-01"));
        period.setEndDate(LocalDate.parse("2026-12-31"));
        period.setActive(true);
        when(periodRepository.save(any(AcademicPeriod.class))).thenReturn(period);
        var result = adminController.createPeriod(body);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().get("name")).isEqualTo("Новый семестр");
    }

    @Test
    void createGroup_ShouldReturnCreated() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Новая группа");
        body.put("periodId", UUID.randomUUID().toString());
        Group group = new Group();
        group.setId(UUID.randomUUID());
        group.setName("Новая группа");
        AcademicPeriod period = new AcademicPeriod();
        period.setId(UUID.randomUUID());
        period.setName("Семестр");
        when(periodRepository.findById(any(UUID.class))).thenReturn(Optional.of(period));
        when(groupRepository.save(any(Group.class))).thenReturn(group);
        var result = adminController.createGroup(body);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().get("name")).isEqualTo("Новая группа");
    }

    @Test
    void createSubject_ShouldReturnCreated() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Новый предмет");
        body.put("groupId", UUID.randomUUID().toString());
        body.put("teacherName", "Учитель");
        body.put("totalHours", 100);
        body.put("weightCoefficient", 0.5);
        Group group = new Group();
        group.setId(UUID.randomUUID());
        group.setName("Группа");
        Subject subject = new Subject();
        subject.setId(UUID.randomUUID());
        subject.setName("Новый предмет");
        when(groupRepository.findById(any(UUID.class))).thenReturn(Optional.of(group));
        when(subjectRepository.save(any(Subject.class))).thenReturn(subject);
        var result = adminController.createSubject(body);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().get("name")).isEqualTo("Новый предмет");
    }

    @Test
    void createAssignment_ShouldReturnCreated() {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Новое задание");
        body.put("subjectId", UUID.randomUUID().toString());
        body.put("dueDate", "2026-12-25T23:59:00");
        body.put("type", "HOMEWORK");
        body.put("weight", 0.33);
        Subject subject = new Subject();
        subject.setId(UUID.randomUUID());
        subject.setName("Предмет");
        Assignment assignment = new Assignment();
        assignment.setId(UUID.randomUUID());
        assignment.setTitle("Новое задание");
        when(subjectRepository.findById(any(UUID.class))).thenReturn(Optional.of(subject));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);
        var result = adminController.createAssignment(body);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().get("title")).isEqualTo("Новое задание");
    }

    @Test
    void getGroup_ShouldReturnGroup() {
        UUID id = UUID.randomUUID();
        AcademicPeriod period = new AcademicPeriod();
        period.setId(UUID.randomUUID());
        period.setName("Семестр");
        Group group = new Group();
        group.setId(id);
        group.setName("Группа");
        group.setPeriod(period);
        when(groupRepository.findById(id)).thenReturn(Optional.of(group));
        var result = adminController.getGroup(id);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().get("id")).isEqualTo(id);
    }

    @Test
    void getSubject_ShouldReturnSubject() {
        UUID id = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        Group group = new Group();
        group.setId(groupId);
        group.setName("Группа");
        Subject subject = new Subject();
        subject.setId(id);
        subject.setName("Предмет");
        subject.setTeacherName("Учитель");
        subject.setTotalHours(100);
        subject.setGroup(group);
        when(subjectRepository.findById(id)).thenReturn(Optional.of(subject));
        var result = adminController.getSubject(id);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().get("id")).isEqualTo(id);
        assertThat(result.getBody().get("groupId")).isEqualTo(groupId);
    }

    @Test
    void getAssignment_ShouldReturnAssignment() {
        UUID id = UUID.randomUUID();
        Subject subject = new Subject();
        subject.setId(UUID.randomUUID());
        subject.setName("Предмет");
        Assignment assignment = new Assignment();
        assignment.setId(id);
        assignment.setTitle("Задание");
        assignment.setSubject(subject);
        assignment.setDueDate(LocalDateTime.now());
        assignment.setType(AssignmentType.HOMEWORK);
        when(assignmentRepository.findById(id)).thenReturn(Optional.of(assignment));
        var result = adminController.getAssignment(id);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().get("id")).isEqualTo(id);
    }

}