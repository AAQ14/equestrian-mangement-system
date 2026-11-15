package com.ga.equestrian.notificationservice.service;

import com.ga.equestrian.notificationservice.dto.response.NotificationResponse;
import com.ga.equestrian.notificationservice.exception.InformationNotFoundException;
import com.ga.equestrian.notificationservice.mapper.NotificationMapper;
import com.ga.equestrian.notificationservice.model.entity.Notification;
import com.ga.equestrian.notificationservice.model.enums.NotificationType;
import com.ga.equestrian.notificationservice.repository.NotificationRepository;
import com.ga.equestrian.notificationservice.security.AuthUser;
import com.ga.equestrian.notificationservice.sse.SseEmitterRegistry;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final AtomicInteger threadCounter = new AtomicInteger(1);
    private final ExecutorService notificationExecuter = Executors.newFixedThreadPool(4,
            runnable -> new Thread(runnable, "notify-" + threadCounter.getAndIncrement()));

    /**
     * Saves a notification for a user and pushes it to the user's open connections.
     * Users who are not connected can read it later through the list endpoint.
     *
     * @param userId the id of the user who receives the notification.
     * @param message the text of the notification.
     * @param notificationType the kind of event the notification is about.
     */
    @Transactional
    public void createAndSend(Long userId, String message, NotificationType notificationType){
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setNotificationType(notificationType);
        Notification savedNotification = notificationRepository.save(notification);

        sseEmitterRegistry.send(userId, notificationMapper.toResponse(savedNotification));
        log.info("Notification {} of type {} created for user {}", savedNotification.getId(), notificationType, userId);
    }

    /**
     * Opens a real-time stream for the signed-in user.
     *
     * @param authUser the signed-in user taken from the JWT.
     * @return the emitter that keeps the connection open.
     * @throws InformationNotFoundException if the user does not exist.
     */
    @Transactional(readOnly = true)
    public SseEmitter openStream(AuthUser authUser){
        return sseEmitterRegistry.register(authUser.userId());
    }

    /**
     * Returns the notifications of the signed-in user, newest first.
     *
     * @param authUser the signed-in user taken from the JWT.
     * @return the user's notifications, empty if there are none.
     * @throws InformationNotFoundException if the user does not exist.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(AuthUser authUser){
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(authUser.userId())
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    /**
     * Creates and sends a notification on the thread pool.
     *
     * @param userId the id of the user who receives the notification.
     * @param message the text of the notification.
     * @param notificationType the kind of the notification.
     */
    public void sendInBackground(Long userId, String message, NotificationType notificationType){
        notificationExecuter.submit(()->{
            try{
                log.info("Notification task started: user={}, thread={}", userId, Thread.currentThread().getName());
                Thread.sleep(500);
                createAndSend(userId, message, notificationType);
            }catch (Exception e){
                log.error("Notification task failed: user={}", userId, e);
            }
        });
    }

    /**
     * Stops the thread pool when the application shuts down.
     */
    @PreDestroy
    public void shutdown() {
        notificationExecuter.shutdown();
    }

}
