package com.ga.equestrian.controller;

import com.ga.equestrian.dto.response.NotificationResponse;
import com.ga.equestrian.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    /**
     * Opens a real-time stream of notifications for the signed-in user.
     * The connection stays open, and the server pushes each new notification through it.
     * Returns 200 with an event stream, and 401 or 403 if the token is missing or invalid.
     *
     * @param authentication the signed-in user, set by the JWT filter.
     * @return the emitter that keeps the connection open.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter openStream(Authentication authentication){
        return notificationService.openStream(authentication.getName());
    }

    /**
     * Returns the notifications of the signed-in user, newest first.
     * Returns 200 on success (an empty list if there are none),
     * 401 or 403 if the token is missing or invalid, and 404 if the user does not exist.
     *
     * @param authentication the signed-in user, set by the JWT filter.
     * @return the user's notifications.
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Authentication authentication){
        return ResponseEntity.status(HttpStatus.OK).body(notificationService.getMyNotifications(authentication.getName()));
    }


}
