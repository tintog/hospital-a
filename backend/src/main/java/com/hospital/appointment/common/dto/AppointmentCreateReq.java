package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AppointmentCreateReq {
    @NotNull(message = "号源ID不能为空")
    private Long slotId;

    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    @NotNull(message = "科室ID不能为空")
    private Long deptId;

    @NotNull(message = "费用不能为空")
    private BigDecimal fee;

    private Long memberId;
}
