package com.ga.equestrian.notificationservice.kafka;

import com.ga.equestrian.notificationservice.dto.event.NotificationEvent;
import com.ga.equestrian.notificationservice.model.entity.UserContact;
import com.ga.equestrian.notificationservice.repository.UserContactRepository;
import com.ga.equestrian.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**z
 * Reads the ticket and tournament events from Kafka and turns into one notification.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketEventConsumer {
    public final NotificationService notificationService;
    private final UserContactRepository userContactRepository;

    @KafkaListener(topics = {"ticket-events", "booking-events"}, groupId = "notification-service")
    public void handle(NotificationEvent event) {
        if(event.userId() == null) {
            //Broadcast: one notification for each active client.
            List<UserContact> clients = userContactRepository.findAllByRoleAndActiveTrue("CLIENT");
            for(UserContact client: clients) {
                notificationService.createAndSend(client.getUserId(), event.message(), event.type());
            }
            log.info("Broadcast {} sent to {} clients", event.type(), clients.size());
            return;
        }
        log.info("Received event {} for user {} (eventId={})", event.type(), event.userId(), event.eventId());
        notificationService.createAndSend(event.userId(), event.message(), event.type());
    }

}
