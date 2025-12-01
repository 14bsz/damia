package com.damai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(title = "OrderSoldSeatListDto", description = "查询某个节目已售座位入参")
public class OrderSoldSeatListDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "programId", type = "Long", description = "节目表id")
    private Long programId;
}
