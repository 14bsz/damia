package com.damai.service;

import com.damai.config.EmailSmtpProperties;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private EmailSmtpProperties emailSmtpProperties;

    public boolean sendEmailCode(String to, String code) {
        if (!emailSmtpProperties.isEnabled()) {
            log.info("邮箱功能未启用,验证码仅在日志中显示 - 邮箱: {}, 验证码: {}", to, code);
            return true;
        }
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", emailSmtpProperties.getHost());
            props.put("mail.smtp.port", String.valueOf(emailSmtpProperties.getPort()));
            props.put("mail.smtp.connectiontimeout", String.valueOf(emailSmtpProperties.getConnectionTimeoutMs()));
            props.put("mail.smtp.timeout", String.valueOf(emailSmtpProperties.getTimeoutMs()));
            props.put("mail.smtp.writetimeout", String.valueOf(emailSmtpProperties.getWriteTimeoutMs()));
            if (emailSmtpProperties.getPort() == 587) {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
            } else {
                props.put("mail.smtp.ssl.enable", "true");
            }

            Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
                @Override
                protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new jakarta.mail.PasswordAuthentication(emailSmtpProperties.getUsername(), emailSmtpProperties.getPassword());
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailSmtpProperties.getUsername(), emailSmtpProperties.getFromName(), StandardCharsets.UTF_8.name()));
            message.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(to)});
            message.setSubject(emailSmtpProperties.getSubject(), StandardCharsets.UTF_8.name());
            String content = "您的验证码为：" + code + "，5分钟内有效";
            message.setText(content, StandardCharsets.UTF_8.name());
            Transport.send(message);
            log.info("邮箱验证码发送成功 - 邮箱: {}", to);
            return true;
        } catch (Exception e) {
            log.error("邮箱验证码发送异常 - 邮箱: {}, 异常信息: {}", to, e.getMessage(), e);
            return false;
        }
    }

    @Async
    public void sendEmailCodeAsync(String to, String code) {
        sendEmailCode(to, code);
    }
}
