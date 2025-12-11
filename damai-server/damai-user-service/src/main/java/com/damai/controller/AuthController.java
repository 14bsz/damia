package com.damai.controller;

import com.damai.common.ApiResponse;
import com.damai.dto.SendEmailCodeDto;
import com.damai.dto.UserEmailCodeLoginDto;
import com.damai.service.UserService;
import com.damai.vo.UserLoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth", description = "认证")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/sendEmailCode")
    public ApiResponse<Boolean> sendEmailCode(@Valid @RequestBody SendEmailCodeDto sendEmailCodeDto) {
        return ApiResponse.ok(userService.sendEmailCode(sendEmailCodeDto));
    }

    @Operation(summary = "邮箱验证码登录")
    @PostMapping("/loginByEmailCode")
    public ApiResponse<UserLoginVo> loginByEmailCode(@Valid @RequestBody UserEmailCodeLoginDto userEmailCodeLoginDto) {
        return ApiResponse.ok(userService.emailCodeLogin(userEmailCodeLoginDto));
    }

    @Operation(summary = "发送邮箱验证码(兼容测试路径)")
    @PostMapping("/email/send")
    public ApiResponse<Boolean> emailSend(@Valid @RequestBody SendEmailCodeDto sendEmailCodeDto) {
        // 默认设置为登录类型
        if (sendEmailCodeDto.getType() == null || sendEmailCodeDto.getType().isEmpty()) {
            sendEmailCodeDto.setType("login");
        }
        return ApiResponse.ok(userService.sendEmailCode(sendEmailCodeDto));
    }
}
