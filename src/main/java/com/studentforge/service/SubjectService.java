package com.studentforge.service;

import com.studentforge.dto.response.SubjectResponse;
import com.studentforge.mapper.UserMapper;
import com.studentforge.repository.SubjectRepository;
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
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserMapper userMapper;

    public Page<SubjectResponse> getSubjectsByGroup(UUID groupId, Pageable pageable) {
        return subjectRepository.findAllByGroupId(groupId, pageable)
                .map(userMapper::toSubjectResponse);
    }
}