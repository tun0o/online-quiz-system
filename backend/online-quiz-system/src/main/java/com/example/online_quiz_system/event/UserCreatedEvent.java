package com.example.online_quiz_system.event;

import lombok.Getter;

/**
 * Thin payload event - chỉ chứa userId để tránh stale data
 */
@Getter
public class UserCreatedEvent {
    private final Long userId;

    public UserCreatedEvent(Long userId) {
        this.userId = userId;
    }
}

