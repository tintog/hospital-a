package com.hospital.appointment.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SlotStatus {
    AVAILABLE(0, "可预约"),
    LOCKED(1, "锁定中"),
    BOOKED(2, "已预约"),
    CANCELLED(3, "已取消"),
    VISITED(4, "已就诊");

    private final Integer code;
    private final String desc;
}
