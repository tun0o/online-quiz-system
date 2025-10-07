package com.example.online_quiz_system.event;

import lombok.Getter;

import java.util.Set;

/**
 * Thin payload event - chỉ chứa userId và changedFields để tránh stale data
 */
@Getter
public class UserUpdatedEvent {
    private final Long userId;
    private final Set<String> changedFields;

    public UserUpdatedEvent(Long userId, Set<String> changedFields) {
        this.userId = userId;
        this.changedFields = changedFields;
    }
}
