package com.studentforge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "teacher_name", length = 150)
    private String teacherName;

    @Column(name = "weight_coefficient", nullable = false, precision = 3, scale = 2)
    private BigDecimal weightCoefficient;

    @Column(name = "total_hours")
    private Integer totalHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Assignment> assignments = new ArrayList<>();
}