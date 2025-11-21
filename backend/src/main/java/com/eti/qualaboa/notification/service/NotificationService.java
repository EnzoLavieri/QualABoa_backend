package com.eti.qualaboa.notification.service;

import com.eti.qualaboa.notification.event.CupomCriadoEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(3600000L);
        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError((e) -> this.emitters.remove(emitter));

        return emitter;
    }

    @EventListener
    public void handleCupomCriado(CupomCriadoEvent event) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        this.emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("novo-cupom")
                        .data(event.getCupom()));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });
        this.emitters.removeAll(deadEmitters);
    }

}
