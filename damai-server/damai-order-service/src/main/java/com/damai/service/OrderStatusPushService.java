package com.damai.service;

import com.damai.vo.OrderStatusPushVo;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class OrderStatusPushService {
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long orderNumber){
        SseEmitter emitter = new SseEmitter(15L * 60 * 1000);
        emitterMap.computeIfAbsent(orderNumber, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(orderNumber, emitter));
        emitter.onTimeout(() -> remove(orderNumber, emitter));
        try {
            emitter.send(SseEmitter.event().name("open").data("ok"));
        } catch (IOException ignored) {}
        return emitter;
    }

    public void send(Long orderNumber, OrderStatusPushVo vo){
        List<SseEmitter> list = emitterMap.get(orderNumber);
        if (list == null || list.isEmpty()) return;
        for (SseEmitter emitter : list){
            try {
                emitter.send(SseEmitter.event().name("orderStatus").data(vo));
            } catch (IOException e) {
                remove(orderNumber, emitter);
            }
        }
    }

    private void remove(Long orderNumber, SseEmitter emitter){
        List<SseEmitter> list = emitterMap.get(orderNumber);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) emitterMap.remove(orderNumber);
        }
    }
}
