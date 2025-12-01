package com.damai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(title = "SeatStatusItemVo", description = "座位状态变更条目")
public class SeatStatusItemVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "ticketCategoryId", type = "Long")
    private Long ticketCategoryId;

    @Schema(name = "zoneName", type = "String")
    private String zoneName;

    @Schema(name = "rowCode", type = "Integer")
    private Integer rowCode;

    @Schema(name = "colCode", type = "Integer")
    private Integer colCode;

    @Schema(name = "sellStatus", type = "Integer")
    private Integer sellStatus;
}
