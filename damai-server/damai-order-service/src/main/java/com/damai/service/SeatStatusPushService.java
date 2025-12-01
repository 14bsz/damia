package com.damai.service;

import com.damai.vo.SeatStatusPushVo;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SeatStatusPushService {
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long programId){
        SseEmitter emitter = new SseEmitter(15L * 60 * 1000);
        emitterMap.computeIfAbsent(programId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(programId, emitter));
        emitter.onTimeout(() -> remove(programId, emitter));
        try {
            emitter.send(SseEmitter.event().name("open").data("ok"));
        } catch (IOException ignored) {}
        return emitter;
    }

    public void send(Long programId, SeatStatusPushVo vo){
        List<SseEmitter> list = emitterMap.get(programId);
        if (list == null || list.isEmpty()) return;
        for (SseEmitter emitter : list){
            try {
                emitter.send(SseEmitter.event().name("seatStatus").data(vo));
            } catch (IOException e) {
                remove(programId, emitter);
            }
        }
    }

    private void remove(Long programId, SseEmitter emitter){
        List<SseEmitter> list = emitterMap.get(programId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) emitterMap.remove(programId);
        }
    }
}

