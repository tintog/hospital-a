package com.hospital.appointment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.common.dto.ScheduleCreateReq;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.common.vo.AppointmentVO;
import com.hospital.appointment.common.vo.StatisticsVO;
import com.hospital.appointment.entity.Schedule;
import com.hospital.appointment.service.AppointmentService;
import com.hospital.appointment.service.ScheduleService;
import com.hospital.appointment.service.SlotService;
import com.hospital.appointment.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "管理后台接口")
public class AdminController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private SlotService slotService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private StatisticsService statisticsService;

    @PostMapping("/schedule/create")
    @Operation(summary = "创建排班")
    public Result<Schedule> createSchedule(@Valid @RequestBody ScheduleCreateReq req) {
        return scheduleService.create(req);
    }

    @PutMapping("/schedule/{id}")
    @Operation(summary = "更新排班")
    public Result<String> updateSchedule(@PathVariable Long id, @Valid @RequestBody ScheduleCreateReq req) {
        return scheduleService.update(id, req);
    }

    @PostMapping("/schedule/{id}/cancel")
    @Operation(summary = "停诊")
    public Result<String> cancelSchedule(@PathVariable Long id) {
        return scheduleService.cancelSchedule(id);
    }

    @GetMapping("/schedule/list")
    @Operation(summary = "排班列表")
    public Result<List<Schedule>> scheduleList(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (doctorId != null) {
            return scheduleService.listByDoctor(doctorId, startDate, endDate);
        }
        if (doctorName != null && !doctorName.isBlank()) {
            return scheduleService.listByDoctorName(doctorName, startDate, endDate);
        }
        return scheduleService.listAll(startDate);
    }

    @PutMapping("/slot/{id}/revoke")
    @Operation(summary = "回撤号源")
    public Result<String> revokeSlot(@PathVariable Long id) {
        return slotService.revokeSlot(id);
    }

    @GetMapping("/appointment/list")
    @Operation(summary = "预约查询")
    public Result<Page<AppointmentVO>> appointmentList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return appointmentService.listAll(status, doctorId, page, size);
    }

    @PostMapping("/appointment/{id}/cancel")
    @Operation(summary = "后台取消预约")
    public Result<String> cancelAppointment(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "管理员取消") String reason) {
        return appointmentService.adminCancelAppointment(id, reason);
    }

    @GetMapping("/statistics/dashboard")
    @Operation(summary = "统计面板")
    public Result<StatisticsVO> dashboard() {
        return statisticsService.getDashboardStats();
    }
}
