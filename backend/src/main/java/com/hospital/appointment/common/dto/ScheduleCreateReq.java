package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 排班创建请求实体类
 * 用于接收前端提交的排班创建请求参数
 * 使用@Data注解自动生成getter、setter等方法
 */
@Data
public class ScheduleCreateReq {
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    @NotNull(message = "出诊日期不能为空")
    private LocalDate workDate;

    @NotNull(message = "班次不能为空")
    private Integer shiftType;

    @NotNull(message = "号源数不能为空")
    private Integer totalSlots;

    private Integer slotDuration = 15;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;
}
