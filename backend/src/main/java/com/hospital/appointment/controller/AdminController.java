package com.hospital.appointment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.common.dto.AdminBlacklistReq;
import com.hospital.appointment.common.dto.AdminDoctorCreateReq;
import com.hospital.appointment.common.dto.AdminDoctorUpdateReq;
import com.hospital.appointment.common.dto.ScheduleCreateReq;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.common.vo.AdminDoctorVO;
import com.hospital.appointment.common.vo.AdminPatientVO;
import com.hospital.appointment.common.vo.AppointmentVO;
import com.hospital.appointment.common.vo.StatisticsVO;
import com.hospital.appointment.entity.Schedule;
import com.hospital.appointment.service.AdminManageService;
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

    @Autowired
    private AdminManageService adminManageService;

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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (doctorId != null) {
            return scheduleService.listByDoctor(doctorId, startDate, endDate);
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

    @GetMapping("/patient/list")
    @Operation(summary = "患者列表")
    public Result<Page<AdminPatientVO>> patientList(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer authStatus,
            @RequestParam(required = false) Integer blacklistStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return adminManageService.listPatients(phone, realName, authStatus, blacklistStatus, page, size);
    }

    @GetMapping("/patient/{id}")
    @Operation(summary = "患者详情")
    public Result<AdminPatientVO> patientDetail(@PathVariable Long id) {
        return adminManageService.patientDetail(id);
    }

    @PutMapping("/patient/{id}/blacklist")
    @Operation(summary = "拉黑患者")
    public Result<String> blacklistPatient(@PathVariable Long id, @Valid @RequestBody AdminBlacklistReq req) {
        return adminManageService.blacklistPatient(id, req);
    }

    @PutMapping("/patient/{id}/unblacklist")
    @Operation(summary = "解除患者黑名单")
    public Result<String> unblacklistPatient(@PathVariable Long id) {
        return adminManageService.unblacklistPatient(id);
    }

    @DeleteMapping("/patient/{id}")
    @Operation(summary = "逻辑删除患者")
    public Result<String> deletePatient(@PathVariable Long id) {
        return adminManageService.deletePatient(id);
    }

    @GetMapping("/doctor/list")
    @Operation(summary = "医生列表")
    public Result<Page<AdminDoctorVO>> doctorList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return adminManageService.listDoctors(name, deptId, title, status, page, size);
    }

    @GetMapping("/doctor/{id}")
    @Operation(summary = "医生详情")
    public Result<AdminDoctorVO> doctorDetail(@PathVariable Long id) {
        return adminManageService.doctorDetail(id);
    }

    @PostMapping("/doctor")
    @Operation(summary = "新增医生")
    public Result<AdminDoctorVO> createDoctor(@Valid @RequestBody AdminDoctorCreateReq req) {
        return adminManageService.createDoctor(req);
    }

    @PutMapping("/doctor/{id}")
    @Operation(summary = "更新医生")
    public Result<String> updateDoctor(@PathVariable Long id, @Valid @RequestBody AdminDoctorUpdateReq req) {
        return adminManageService.updateDoctor(id, req);
    }

    @DeleteMapping("/doctor/{id}")
    @Operation(summary = "逻辑删除医生")
    public Result<String> deleteDoctor(@PathVariable Long id) {
        return adminManageService.deleteDoctor(id);
    }

    @PostMapping("/doctor/backfill-accounts")
    @Operation(summary = "一键补齐医生账号")
    public Result<String> backfillDoctorAccounts() {
        return adminManageService.backfillDoctorAccounts();
    }
}
