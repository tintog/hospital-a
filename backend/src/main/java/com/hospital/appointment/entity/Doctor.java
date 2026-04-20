package com.hospital.appointment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("doctor")
public class Doctor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private Long deptId;
    private String title;
    private String specialty;
    private String introduction;
    private String avatar;
    @TableLogic
    private Integer deleted;
}
