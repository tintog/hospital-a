package com.hospital.appointment.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AppointmentStatus {
    PENDING_PAY(0, "待支付"),
    CONFIRMED(1, "已确认"),
    CANCELLED(2, "已取消"),
    COMPLETED(3, "已完成"),
    NO_SHOW(4, "爽约");

    private final Integer code;
    private final String desc;
}
