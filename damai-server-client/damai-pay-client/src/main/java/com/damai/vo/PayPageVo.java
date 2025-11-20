package com.damai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PayPageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "body", type = "String", description = "支付页HTML")
    private String body;

    @Schema(name = "outTradeNo", type = "String", description = "支付outTradeNo")
    private String outTradeNo;
}