package com.hospital.appointment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("patient")
public class Patient {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String password;
    private String realName;
    private String idCard;
    private Integer authStatus;
    private LocalDateTime blacklistEndTime;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
