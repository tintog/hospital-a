package com.hospital.appointment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("slot")
public class Slot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long scheduleId;
    private Long doctorId;
    private Long deptId;
    private String slotNo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal fee;
    private Integer status;
    private Long lockedBy;
    private LocalDateTime lockedExpireTime;
    @Version
    private Integer version;
    @TableLogic
    private Integer deleted;
}
