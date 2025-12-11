package com.damai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(title = "SendEmailCodeDto", description = "发送邮箱验证码")
public class SendEmailCodeDto {

    @Schema(name = "email", type = "String", description = "邮箱地址", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    @Email
    private String email;

    @Schema(name = "type", type = "String", description = "验证码类型 login/register", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    private String type;
}

