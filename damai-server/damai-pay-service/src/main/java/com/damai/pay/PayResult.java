package com.damai.pay;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description: 支付结果
 **/
@Data
public class PayResult {

    private final boolean success;

    private final String body;

    private final String outTradeNo;

    public PayResult(boolean success, String body, String outTradeNo) {
        this.success = success;
        this.body = body;
        this.outTradeNo = outTradeNo;
    }
}
