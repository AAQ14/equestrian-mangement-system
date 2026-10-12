package com.ga.equestrian.service;

import com.ga.equestrian.dto.response.NotificationResponse;
import com.ga.equestrian.exception.InformationNotFoundException;
import com.ga.equestrian.mapper.NotificationMapper;
import com.ga.equestrian.model.entity.Notification;
import com.ga.equestrian.model.entity.User;
import com.ga.equestrian.model.enums.NotificationType;
import com.ga.equestrian.repository.NotificationRepository;
import com.ga.equestrian.repository.UserRepository;
import com.ga.equestrian.sse.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final SseEmitterRegistry sseEmitterRegistry;

    /**
     * Saves a notification for a user and pushes it to the user's open connections.
     * Users who are not connected can read it later through the list endpoint.
     *
     * @param user the user who receives the notification.
     * @param message the text of the notification.
     * @param notificationType the kind of event the notification is about.
     */
    @Transactional
    public void createAndSend(User user, String message, NotificationType notificationType){
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setNotificationType(notificationType);
        Notification savedNotification = notificationRepository.save(notification);

        sseEmitterRegistry.send(user.getId(), notificationMapper.toResponse(savedNotification));
        log.info("Notification {} of type {} created for user {}", savedNotification.getId(), notificationType, user.getId());
    }

    /**
     * Opens a real-time stream for the signed-in user.
     *
     * @param email the email of the signed-in user, taken from the JWT.
     * @return the emitter that keeps the connection open.
     * @throws InformationNotFoundException if the user does not exist.
     */
    @Transactional(readOnly = true)
    public SseEmitter openStream(String email){
        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new InformationNotFoundException("User with this email is not found: " + email)
        );
        return sseEmitterRegistry.register(user.getId());
    }

    /**
     * Returns the notifications of the signed-in user, newest first.
     *
     * @param email the email of the signed-in user, taken from the JWT.
     * @return the user's notifications, empty if there are none.
     * @throws InformationNotFoundException if the user does not exist.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotification(String email){
        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new InformationNotFoundException("User with this email is not found: " + email)
        );
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }


}
