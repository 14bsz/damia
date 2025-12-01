package com.damai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(title = "SoldSeatSimpleVo", description = "已售座位简要信息")
public class SoldSeatSimpleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "ticketCategoryId", type = "Long", description = "节目票档id")
    private Long ticketCategoryId;

    @Schema(name = "zoneName", type = "String", description = "区域名称")
    private String zoneName;

    @Schema(name = "rowCode", type = "Integer", description = "排号")
    private Integer rowCode;

    @Schema(name = "colCode", type = "Integer", description = "列号")
    private Integer colCode;
}
