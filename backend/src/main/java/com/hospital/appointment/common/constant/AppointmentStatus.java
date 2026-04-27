package com.hospital.appointment.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预约状态枚举类
 * 使用@Getter和@AllArgsConstructor注解自动生成getter方法和全参数构造方法
 */
@Getter
@AllArgsConstructor
public enum AppointmentStatus {

    // 预约状态枚举值，包含状态码和对应描述
    PENDING_PAY(0, "待支付"),    // 待支付状态
    CONFIRMED(1, "已确认"),      // 已确认状态
    CANCELLED(2, "已取消"),      // 已取消状态
    COMPLETED(3, "已完成"),      // 已完成状态
    NO_SHOW(4, "爽约");         // 爽约状态

    // 状态码，使用Integer类型以支持null值
    private final Integer code;
    // 状态描述，用于展示和说明
    private final String desc;
}
