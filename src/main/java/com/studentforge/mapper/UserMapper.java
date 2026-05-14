package com.studentforge.mapper;

import com.studentforge.dto.response.AssignmentResponse;
import com.studentforge.dto.response.SubjectResponse;
import com.studentforge.dto.response.UserProfileResponse;
import com.studentforge.entity.Assignment;
import com.studentforge.entity.Subject;
import com.studentforge.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "university", source = "studentProfile.university")
    @Mapping(target = "major", source = "studentProfile.major")
    @Mapping(target = "groupName", source = "studentProfile.groupName")
    @Mapping(target = "avatarUrl", source = "studentProfile.avatarUrl")
    UserProfileResponse toUserProfileResponse(User user);

    @Mapping(target = "groupId", source = "group.id")
    SubjectResponse toSubjectResponse(Subject subject);

    @Mapping(target = "subjectId", source = "subject.id")
    @Mapping(target = "subjectName", source = "subject.name")
    @Mapping(target = "score", expression = "java(assignment.getGrades().isEmpty() ? null : assignment.getGrades().get(0).getScore())")
    AssignmentResponse toAssignmentResponse(Assignment assignment);

}