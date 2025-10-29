package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.entity.Notification;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotificationsForUser(@AuthenticationPrincipal UserPrincipal userPrincipal){
        List<Notification> notifications = notificationService.getAllNotifications(userPrincipal.getId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadNotificationsCount(@AuthenticationPrincipal UserPrincipal userPrincipal){
        long count = notificationService.getUnreadNotificationCount(userPrincipal.getId());
        return ResponseEntity.ok(count);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long notificationId,
                                                       @AuthenticationPrincipal UserPrincipal userPrincipal){
        notificationService.markNotificationAsRead(notificationId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead(@AuthenticationPrincipal UserPrincipal userPrincipal){
        notificationService.markAllNotificationAsRead(userPrincipal.getId());
        return ResponseEntity.ok().build();
    }
}
