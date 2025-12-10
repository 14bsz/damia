package com.damai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.damai.config.SmsSpugProperties;
import com.damai.client.SpugClient;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @description: 短信服务 - 使用Spug推送
 **/
@Slf4j
@Service
public class SmsService {

    @Autowired
    private SmsSpugProperties smsSpugProperties;

    @Autowired
    private SpugClient spugClient;

    /**
     * 发送短信验证码
     * 
     * @param phone 手机号
     * @param code  验证码
     * @return 是否发送成功
     */
    public boolean sendSmsCode(String phone, String code) {
        // 如果短信功能未启用,只打印日志
        if (!smsSpugProperties.isEnabled()) {
            log.info("短信功能未启用,验证码仅在日志中显示 - 手机号: {}, 验证码: {}", phone, code);
            return true;
        }

        try {
            java.util.Map<String, String> body = new java.util.HashMap<>();
            body.put(smsSpugProperties.getCodeVariable(), code);
            if (smsSpugProperties.isSendName()) {
                body.put(smsSpugProperties.getNameVariable(), smsSpugProperties.getName());
            }
            if (smsSpugProperties.isSendNumber()) {
                String normalizedNumber = normalizeNumber(smsSpugProperties.getNumber());
                body.put(smsSpugProperties.getNumberVariable(), normalizedNumber);
            }
            body.put("targets", phone);
            String result = spugClient.sendPost(smsSpugProperties.getTemplateId(), body);
            log.info("短信验证码发送成功 - 手机号: {}, 响应内容: {}", phone, result);
            return true;
        } catch (Exception e) {
            log.error("短信验证码发送异常 - 手机号: {}, 验证码: {}, 异常信息: {}", phone, code, e.getMessage(), e);
            return false;
        }
    }
    
    private String normalizeNumber(String value) {
        String digits = value.replaceAll("\\D", "");
        if (digits.length() == 0) {
            String v = value.trim();
            if (v.contains("十一")) return "11";
            if (v.contains("十二")) return "12";
            if (v.contains("十三")) return "13";
            if (v.contains("十四")) return "14";
            if (v.contains("十五")) return "15";
            if (v.contains("十六")) return "16";
            if (v.contains("十七")) return "17";
            if (v.contains("十八")) return "18";
            if (v.contains("十九")) return "19";
            if (v.contains("十")) return "10";
            if (v.contains("一")) return "01";
            if (v.contains("二")) return "02";
            if (v.contains("两")) return "02";
            if (v.contains("三")) return "03";
            if (v.contains("四")) return "04";
            if (v.contains("五")) return "05";
            if (v.contains("六")) return "06";
            if (v.contains("七")) return "07";
            if (v.contains("八")) return "08";
            if (v.contains("九")) return "09";
            return "01";
        }
        if (digits.length() == 1) {
            return "0" + digits;
        }
        return digits.substring(0, 2);
    }

    /**
     * 发送通知短信
     * 
     * @param phone   手机号
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendNotification(String phone, String message) {
        if (!smsSpugProperties.isEnabled()) {
            log.info("短信功能未启用,通知仅在日志中显示 - 手机号: {}, 消息: {}", phone, message);
            return true;
        }

        try {
            String result = spugClient.sendGet(smsSpugProperties.getTemplateId(), message, phone);
            log.info("通知短信发送成功 - 手机号: {}, 响应内容: {}", phone, result);
            return true;
        } catch (Exception e) {
            log.error("通知短信发送异常 - 手机号: {}, 异常信息: {}", phone, e.getMessage(), e);
            return false;
        }
    }
}
