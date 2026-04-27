package com.hospital.appointment.common.vo;

import lombok.Data;

/**
 * 医生档案值对象(VO)
 * 用于封装和传输医生相关的数据信息
 */
@Data
public class DoctorProfileVO {
    // 用户ID，关联系统用户表的主键
    private Long userId;
    // 医生ID，关联医生信息表的主键
    private Long doctorId;
    // 用户名，用于系统登录的账号
    private String username;
    // 真实姓名，医生的真实姓名
    private String realName;
    // 科室ID，关联科室信息表的主键
    private Long deptId;
    // 科室名称，医生所属科室的名称
    private String deptName;
    // 职称，医生的专业技术职称
    private String title;
    // 专长，医生的专业特长描述
    private String specialty;
}
