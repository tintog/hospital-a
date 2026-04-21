package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminBlacklistReq {

    @NotNull(message = "拉黑天数不能为空")
    @Min(value = 1, message = "拉黑天数最少1天")
    @Max(value = 365, message = "拉黑天数最多365天")
    private Integer days;
}
