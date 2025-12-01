package com.damai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@Schema(title="OrderStatusPushVo", description ="订单状态推送")
public class OrderStatusPushVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name ="orderNumber", type ="Long", description ="订单编号")
    private Long orderNumber;

    @Schema(name ="orderStatus", type ="Integer", description ="订单状态 1未支付 2已取消 3已支付 4已退单")
    private Integer orderStatus;

    @Schema(name ="payOrderTime", type ="Date")
    private Date payOrderTime;

    @Schema(name ="cancelOrderTime", type ="Date")
    private Date cancelOrderTime;
}
