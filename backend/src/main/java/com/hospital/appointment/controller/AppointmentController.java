package com.hospital.appointment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.common.dto.AppointmentCreateReq;
import com.hospital.appointment.common.dto.PaymentCallbackReq;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.common.vo.AppointmentVO;
import com.hospital.appointment.common.vo.OrderVO;
import com.hospital.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointment")
@Tag(name = "预约接口")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/create")
    @Operation(summary = "创建预约")
    public Result<OrderVO> create(@Valid @RequestBody AppointmentCreateReq req, Authentication authentication) {
        Long patientId = (Long) authentication.getPrincipal();
        return appointmentService.createAppointment(req, patientId);
    }

    @GetMapping("/list")
    @Operation(summary = "我的预约列表")
    public Result<Page<AppointmentVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long patientId = (Long) authentication.getPrincipal();
        return appointmentService.listByPatient(patientId, status, page, size);
    }

    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消预约")
    public Result<String> cancel(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "") String reason,
            Authentication authentication) {
        Long patientId = (Long) authentication.getPrincipal();
        return appointmentService.cancelAppointment(id, patientId, reason);
    }
}
