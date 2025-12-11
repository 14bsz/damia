package com.damai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "email.smtp")
public class EmailSmtpProperties {
    private boolean enabled = false;
    private String host = "smtp.qq.com";
    private int port = 465;
    private String username;
    private String password;
    private String fromName = "票务平台";
    private String subject = "验证码登录";
    private int connectionTimeoutMs = 5000;
    private int timeoutMs = 5000;
    private int writeTimeoutMs = 5000;
}
