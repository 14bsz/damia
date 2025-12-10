package com.damai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(title = "UserSmsLoginDto", description = "短信验证码登录入参")
public class UserSmsLoginDto {

    @Schema(name = "code", type = "String", description = "渠道编码", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private String code;

    @Schema(name = "mobile", type = "String", description = "手机号", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private String mobile;

    @Schema(name = "smsCode", type = "String", description = "短信验证码", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private String smsCode;
}
