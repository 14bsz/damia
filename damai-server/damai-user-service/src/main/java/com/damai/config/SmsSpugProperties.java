package com.damai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sms.spug")
public class SmsSpugProperties {
    private String host = "https://push.spug.cc";
    private String templateId = "Eov6p8keXKjx9NO1";
    private boolean enabled = true;
    private String codeVariable = "key2";
    private String nameVariable = "key1";
    private String name = "推送助手";
    private String numberVariable = "key3";
    private String number = "1分钟";
    private boolean sendName = false;
    private boolean sendNumber = false;
    private String pushUrl = "https://push.spug.cc/send/%s?code=%s&targets=%s";
    private String sendPostUrl = "https://push.spug.cc/send/%s";
}
