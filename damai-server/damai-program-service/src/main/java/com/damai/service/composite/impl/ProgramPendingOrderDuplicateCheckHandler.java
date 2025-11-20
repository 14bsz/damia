package com.damai.service.composite.impl;

import com.damai.client.OrderClient;
import com.damai.common.ApiResponse;
import com.damai.dto.AccountOrderCountDto;
import com.damai.dto.ProgramOrderCreateDto;
import com.damai.enums.BaseCode;
import com.damai.exception.DaMaiFrameException;
import com.damai.service.composite.AbstractProgramCheckHandler;
import com.damai.vo.AccountOrderCountVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProgramPendingOrderDuplicateCheckHandler extends AbstractProgramCheckHandler {

    @Autowired
    private OrderClient orderClient;

    @Override
    protected void execute(final ProgramOrderCreateDto programOrderCreateDto) {
        AccountOrderCountDto accountOrderCountDto = new AccountOrderCountDto();
        accountOrderCountDto.setUserId(programOrderCreateDto.getUserId());
        accountOrderCountDto.setProgramId(programOrderCreateDto.getProgramId());
        ApiResponse<AccountOrderCountVo> apiResponse = orderClient.accountPendingOrderCount(accountOrderCountDto);
        if (!Objects.equals(apiResponse.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new DaMaiFrameException(apiResponse);
        }
        AccountOrderCountVo data = apiResponse.getData();
        if (data != null && data.getCount() != null && data.getCount() > 0) {
            throw new DaMaiFrameException(BaseCode.PENDING_SAME_PROGRAM_ORDER_EXIST);
        }
    }

    @Override
    public Integer executeParentOrder() {
        return 1;
    }

    @Override
    public Integer executeTier() {
        return 2;
    }

    @Override
    public Integer executeOrder() {
        return 3;
    }
}