package com.ga.equestrian.repository;

import com.ga.equestrian.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * Finds all the notifications of a user, newest first.
     *
     * @param userId the id of the user.
     * @return the user's notifications, or an empty list if there are none.
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}
