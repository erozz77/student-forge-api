package com.studentforge.service;

import com.studentforge.dto.response.AssignmentResponse;
import com.studentforge.mapper.UserMapper;
import com.studentforge.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserMapper userMapper;

    public Page<AssignmentResponse> getAssignmentsBySubject(UUID subjectId, Pageable pageable) {
        return assignmentRepository.findAllBySubjectId(subjectId, pageable)
                .map(userMapper::toAssignmentResponse);
    }

    public Page<AssignmentResponse> getAssignmentsBySubjectAndStatus(UUID subjectId, String status, Pageable pageable) {
        return assignmentRepository.findAllBySubjectIdAndStatus(subjectId, status, pageable)
                .map(userMapper::toAssignmentResponse);
    }
}