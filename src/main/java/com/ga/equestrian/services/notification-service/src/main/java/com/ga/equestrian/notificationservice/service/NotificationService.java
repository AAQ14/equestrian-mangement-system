package com.ga.equestrian.notificationservice.service;

import com.ga.equestrian.notificationservice.dto.response.NotificationResponse;
import com.ga.equestrian.notificationservice.exception.InformationNotFoundException;
import com.ga.equestrian.notificationservice.mapper.NotificationMapper;
import com.ga.equestrian.notificationservice.model.entity.Notification;
import com.ga.equestrian.notificationservice.model.enums.NotificationType;
import com.ga.equestrian.notificationservice.repository.NotificationRepository;
import com.ga.equestrian.notificationservice.repository.UserContactRepository;
import com.ga.equestrian.notificationservice.security.AuthUser;
import com.ga.equestrian.notificationservice.sse.SseEmitterRegistry;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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
    private final UserContactRepository userContactRepository;
    private final EmailService emailService;
    private final AtomicInteger threadCounter = new AtomicInteger(1);
    private final ExecutorService notificationExecuter = Executors.newFixedThreadPool(4,
            runnable -> new Thread(runnable, "notify-" + threadCounter.getAndIncrement()));

    private static final Set<NotificationType> EMAIL_TYPES = EnumSet.of(
            NotificationType.BOOKING_CONFIRMED,
            NotificationType.BOOKING_CANCELLED,
            NotificationType.SESSION_UPDATED,
            NotificationType.SESSION_CANCELLED,
            NotificationType.TICKET_PURCHASED,
            NotificationType.TICKET_CANCELLED,
            NotificationType.TICKET_SEAT_CHANGED,
            NotificationType.TOURNAMENT_CREATED,
            NotificationType.TOURNAMENT_UPDATED,
            NotificationType.TOURNAMENT_CANCELLED
    );

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
        if (EMAIL_TYPES.contains(notificationType)) {
            sendEmailInBackground(userId, notificationType, message);
        }
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
     * Sends the notification email on the thread pool, so a slow mail server never blocks Kafka.
     * The address comes from the local UserContact copy, so no call to user-service is needed.
     *
     * @param userId the id of the user to email.
     * @param type the kind of the notification.
     * @param message the text of the notification.
     */
    private void sendEmailInBackground(Long userId, NotificationType type, String message) {
        log.info("Email queued: user={}, type={}", userId, type);
        notificationExecuter.submit(() ->{
        try {
            userContactRepository.findById(userId).ifPresentOrElse(
                    contact -> emailService.sendNotificationEmail(contact.getEmail(), contact.getFirstName(), type, message),
                    () -> log.warn("No contact details for user {}, email not sent", userId));
        } catch (Exception ex) {
            log.error("Email task failed: user={}, type={}", userId, type, ex);
        }});
    }

    /**
     * Stops the thread pool when the application shuts down.
     */
    @PreDestroy
    public void shutdown() {
        notificationExecuter.shutdown();
    }

}
