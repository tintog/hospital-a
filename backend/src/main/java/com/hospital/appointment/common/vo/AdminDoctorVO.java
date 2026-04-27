package com.hospital.appointment.common.vo;

import lombok.Data;

/**管理员视角下的医生信息视图对象*/
@Data
public class AdminDoctorVO {
    private Long id;            // 医生ID
    private Long userId;        // 关联的用户ID
    private String username;    // 用户名
    private String name;        // 医生姓名
    private Long deptId;        // 所属部门ID
    private String deptName;    // 所属部门名称
    private String title;       // 职称
    private String specialty;   // 专业特长
    private String introduction; // 简介
    private Integer status;     // 状态
}
