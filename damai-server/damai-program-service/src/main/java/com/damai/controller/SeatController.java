package com.damai.controller;

import com.damai.common.ApiResponse;
import com.damai.dto.SeatBatchAddDto;
import com.damai.dto.SeatListDto;
import com.damai.service.SeatService;
import com.damai.vo.SeatRelateInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 座位 控制层
 **/
@RestController
@RequestMapping("/program/seat")
@Tag(name = "program-seat", description = "节目座位")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @Operation(summary = "座位相关信息")
    @PostMapping(value = "/relate/info")
    public ApiResponse<SeatRelateInfoVo> relateInfo(@Valid @RequestBody SeatListDto seatListDto) {
        return ApiResponse.ok(seatService.relateInfo(seatListDto));
    }

    @Operation(summary = "批量添加座位")
    @PostMapping(value = "/batch/add")
    public ApiResponse<Boolean> batchAdd(@Valid @RequestBody SeatBatchAddDto seatBatchAddDto) {
        return ApiResponse.ok(seatService.batchAdd(seatBatchAddDto));
    }
}

