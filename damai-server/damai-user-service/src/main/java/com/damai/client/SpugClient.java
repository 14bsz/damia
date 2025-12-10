package com.damai.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "spugClient", url = "${sms.spug.host:https://push.spug.cc}")
public interface SpugClient {
    @PostMapping("/send/{templateId}")
    String sendPost(@PathVariable("templateId") String templateId, @RequestBody Map<String, String> body);

    @GetMapping("/send/{templateId}")
    String sendGet(@PathVariable("templateId") String templateId,
                   @RequestParam("code") String code,
                   @RequestParam("targets") String targets);
}
