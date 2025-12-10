package com.damai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(title = "SendSmsCodeDto", description = "发送短信验证码入参")
public class SendSmsCodeDto {

    @Schema(name = "mobile", type = "String", description = "手机号", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private String mobile;

    @Schema(name = "type", type = "String", description = "类型: login 或 register", requiredMode = RequiredMode.REQUIRED)
    @NotNull
    private String type;
}
