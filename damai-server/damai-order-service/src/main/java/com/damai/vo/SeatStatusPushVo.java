package com.damai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(title = "SeatStatusPushVo", description = "座位状态推送数据")
public class SeatStatusPushVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "programId", type = "Long")
    private Long programId;

    @Schema(name = "items", description = "状态变更列表")
    private List<SeatStatusItemVo> items;
}

