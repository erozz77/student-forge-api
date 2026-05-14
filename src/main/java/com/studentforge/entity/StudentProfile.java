package com.studentforge.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 255)
    private String university;

    @Column(length = 150)
    private String major;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "group_name", length = 100)
    private String groupName;
}