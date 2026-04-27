package com.hospital.appointment.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端患者视图对象
 * 用于在管理端展示患者相关信息的数据传输对象
 */
@Data
public class AdminPatientVO {
    private Long id;                    // 患者ID
    private String phone;               // 患者手机号
    private String realName;            // 患者真实姓名
    private Integer authStatus;         // 认证状态
    private LocalDateTime blacklistEndTime;  // 黑名单结束时间
    private LocalDateTime createdAt;    // 创建时间
    private Long recentAppointmentCount; // 最近预约次数
}
