package com.hospital.appointment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long patientId;
    private Long memberId;
    private Long slotId;
    private Long doctorId;
    private Long deptId;
    private BigDecimal fee;
    private Integer status;
    private LocalDateTime payTime;
    private String cancelReason;
    private Integer cancelBy;
    private Integer noShowFlag;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
