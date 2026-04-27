package com.hospital.appointment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Schedule类，用于表示医生排班信息
 * 使用了Lombok的@Data注解自动生成getter、setter等方法
 * 使用了MyBatis-Plus的@TableName注解指定对应的数据库表名
 */
@Data
@TableName("schedule")
public class Schedule {
    /** 主键ID，使用自增策略
     * 使用了MyBatis-Plus的@TableId注解，并指定IdType.AUTO为自增类型*/
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 医生ID，关联医生表的主键*/
    private Long doctorId;
    /** 工作日期，使用LocalDate类型存储日期信息*/
    private LocalDate workDate;
    /** 班次类型，用于标识不同的班次（如上午、下午、夜间等）*/
    private Integer shiftType;
    /** 总预约号槽数量，表示该班次可预约的总人数*/
    private Integer totalSlots;
    /*** 已预约号槽数量，表示该班次已被预约的人数*/
    private Integer bookedSlots;
    /** 号程时长（分钟），表示每个预约号程的时间长度*/
    private Integer slotDuration;
    private LocalTime startTime;

    private LocalTime endTime;

    private Integer status;

    @TableLogic

    private Integer deleted;
    private LocalDateTime createdAt;
}
    /** 创建时间，记录排班信息的创建时间*/
