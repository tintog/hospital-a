package com.hospital.appointment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("schedule")
public class Schedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private LocalDate workDate;
    private Integer shiftType;
    private Integer totalSlots;
    private Integer bookedSlots;
    private Integer slotDuration;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer status;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
}
