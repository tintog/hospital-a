package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 就诊状态更新请求类
 * 用于封装更新就诊状态所需的请求参数
 */
@Data
public class VisitStatusUpdateReq {

    @NotNull(message = "就诊状态不能为空")
    private Integer visitStatus;
}
