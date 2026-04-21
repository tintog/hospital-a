package com.hospital.appointment.common.vo;

import lombok.Data;

@Data
public class DoctorProfileVO {
    private Long userId;
    private Long doctorId;
    private String username;
    private String realName;
    private Long deptId;
    private String deptName;
    private String title;
    private String specialty;
}
