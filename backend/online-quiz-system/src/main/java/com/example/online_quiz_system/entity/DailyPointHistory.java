package com.example.online_quiz_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_point_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyPointHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "source_id")
    private Long sourceId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
