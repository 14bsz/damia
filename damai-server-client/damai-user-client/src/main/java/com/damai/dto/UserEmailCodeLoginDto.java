package com.damai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(title = "UserEmailCodeLoginDto", description = "邮箱验证码登录")
public class UserEmailCodeLoginDto {

    @Schema(name = "code", type = "String", description = "渠道code 0001:pc网站", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String code;

    @Schema(name = "email", type = "String", description = "用户邮箱", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    @Email
    private String email;

    @Schema(name = "emailCode", type = "String", description = "邮箱验证码", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String emailCode;
}

