package com.ga.equestrian.sse;

import com.ga.equestrian.dto.response.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class SseEmitterRegistry {
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private static final long TIMEOUT_MS  = 30 * 60 * 1000L;

    /**
     * Opens a new SSE connection for a user and remembers it.
     * The connection is forgotten when it completes, times out or fails.
     *
     * @param userId the id of the user who opens the stream.
     * @return the emitter that keeps the HTTP connection open.
     */

    public SseEmitter register(Long userId){
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.computeIfAbsent(userId, id-> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(()->remove(userId, emitter));
        emitter.onTimeout(()->remove(userId, emitter));
        emitter.onError(ex->remove(userId, emitter));
        return emitter;
    }

    /**
     * Pushes a notification to every open connection of a user.
     * Connections that can no longer be written to are removed.
     *
     * @param userId the id of the user who receives the event.
     * @param data the notification to send.
     */
    public void send(Long userId, NotificationResponse data){
        for(SseEmitter emitter: emitters.getOrDefault(userId, List.of())){
            try{
                emitter.send(SseEmitter.event().name("notification").data(data));
            }catch (IOException ex){
                remove(userId, emitter);
            }
        }
    }


    /**
     * Forgets a connection of a user.
     *
     * @param userId the id of the user.
     * @param emitter the connection to remove.
     */
    private void remove(Long userId, SseEmitter emitter){
        List<SseEmitter> userEmitters = emitters.get(userId);
        if(userEmitters != null) {
            userEmitters.remove(emitter);
        }
    }
}
