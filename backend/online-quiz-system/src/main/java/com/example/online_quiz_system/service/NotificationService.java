package com.example.online_quiz_system.service;

import com.example.online_quiz_system.entity.Notification;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.enums.NotificationType;
import com.example.online_quiz_system.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(User recipient, String message, String link, NotificationType notificationType){
        Notification notification = new Notification(recipient, message, link, notificationType);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getAllNotifications(Long userId, Pageable pageable){
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId){
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId, Long userId){
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));


        if(!notification.getRecipient().getId().equals(userId))
            throw new SecurityException("User cannot access this notification");

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllNotificationAsRead(Long userId){
        notificationRepository.markAllAsReadForRecipient(userId);
    }
}
