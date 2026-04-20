package com.hospital.appointment.controller;

import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctor")
@Tag(name = "医生接口")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/list")
    @Operation(summary = "医生列表")
    public Result<List<Doctor>> list(
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String title) {
        return doctorService.list(deptId, title);
    }

    @GetMapping("/{id}")
    @Operation(summary = "医生详情")
    public Result<Doctor> detail(@PathVariable Long id) {
        return doctorService.detail(id);
    }
}
