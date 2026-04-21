package com.hospital.appointment.common.vo;

import lombok.Data;

@Data
public class AdminDoctorVO {
    private Long id;
    private Long userId;
    private String username;
    private String name;
    private Long deptId;
    private String deptName;
    private String title;
    private String specialty;
    private String introduction;
    private Integer status;
}
