package com.studentforge.mapper;

import com.studentforge.dto.response.AssignmentResponse;
import com.studentforge.dto.response.SubjectResponse;
import com.studentforge.dto.response.UserProfileResponse;
import com.studentforge.entity.*;
import com.studentforge.enums.AssignmentStatus;
import com.studentforge.enums.AssignmentType;
import com.studentforge.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void toUserProfileResponse_ShouldMapAllFields() {
        var user = User.builder()
                .email("test@test.com")
                .firstName("Иван")
                .lastName("Иванов")
                .role(Role.USER)
                .build();
        user.setId(UUID.randomUUID());
        var profile = new StudentProfile();
        profile.setUniversity("ЯГТУ");
        profile.setMajor("ИС");
        profile.setGroupName("ЦИС-23");
        user.setStudentProfile(profile);

        UserProfileResponse result = userMapper.toUserProfileResponse(user);

        assertThat(result.email()).isEqualTo("test@test.com");
        assertThat(result.firstName()).isEqualTo("Иван");
        assertThat(result.lastName()).isEqualTo("Иванов");
        assertThat(result.role()).isEqualTo("USER");
        assertThat(result.university()).isEqualTo("ЯГТУ");
        assertThat(result.major()).isEqualTo("ИС");
        assertThat(result.groupName()).isEqualTo("ЦИС-23");
    }

    @Test
    void toSubjectResponse_ShouldMapAllFields() {
        var group = new Group();
        group.setId(UUID.randomUUID());
        var subject = Subject.builder()
                .name("Java")
                .teacherName("Пашичев ВС")
                .weightCoefficient(new BigDecimal("0.5"))
                .totalHours(100)
                .group(group)
                .build();
        subject.setId(UUID.randomUUID());

        SubjectResponse result = userMapper.toSubjectResponse(subject);

        assertThat(result.name()).isEqualTo("Java");
        assertThat(result.teacherName()).isEqualTo("Пашичев ВС");
        assertThat(result.weightCoefficient()).isEqualByComparingTo("0.5");
        assertThat(result.totalHours()).isEqualTo(100);
        assertThat(result.groupId()).isEqualTo(group.getId());
    }

    @Test
    void toAssignmentResponse_ShouldMapAllFields() {
        var subject = Subject.builder().name("Java").build();
        subject.setId(UUID.randomUUID());
        var assignment = Assignment.builder()
                .title("Лаб 1")
                .description("Описание")
                .dueDate(LocalDateTime.of(2026, 3, 15, 23, 59))
                .status(AssignmentStatus.PENDING)
                .type(AssignmentType.LAB)
                .weight(new BigDecimal("0.33"))
                .subject(subject)
                .build();
        assignment.setId(UUID.randomUUID());

        AssignmentResponse result = userMapper.toAssignmentResponse(assignment);

        assertThat(result.title()).isEqualTo("Лаб 1");
        assertThat(result.description()).isEqualTo("Описание");
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.type()).isEqualTo("LAB");
        assertThat(result.weight()).isEqualByComparingTo("0.33");
        assertThat(result.subjectId()).isEqualTo(subject.getId());
        assertThat(result.subjectName()).isEqualTo("Java");
    }
}