package com.hospital.appointment.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminPatientVO {
    private Long id;
    private String phone;
    private String realName;
    private Integer authStatus;
    private LocalDateTime blacklistEndTime;
    private LocalDateTime createdAt;
    private Long recentAppointmentCount;
}
