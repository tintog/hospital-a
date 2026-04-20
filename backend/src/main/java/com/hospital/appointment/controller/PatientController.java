package com.hospital.appointment.controller;

import com.hospital.appointment.common.dto.MemberReq;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.entity.Patient;
import com.hospital.appointment.entity.PatientMember;
import com.hospital.appointment.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patient")
@Tag(name = "患者接口")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @GetMapping("/profile")
    @Operation(summary = "获取个人信息")
    public Result<Patient> getProfile(Authentication authentication) {
        Long patientId = (Long) authentication.getPrincipal();
        return patientService.getProfile(patientId);
    }

    @GetMapping("/members")
    @Operation(summary = "获取就诊人列表")
    public Result<List<PatientMember>> getMembers(Authentication authentication) {
        Long patientId = (Long) authentication.getPrincipal();
        return patientService.getMembers(patientId);
    }

    @PostMapping("/members")
    @Operation(summary = "添加就诊人")
    public Result<PatientMember> addMember(@Valid @RequestBody MemberReq req, Authentication authentication) {
        Long patientId = (Long) authentication.getPrincipal();
        return patientService.addMember(patientId, req);
    }

    @DeleteMapping("/members/{id}")
    @Operation(summary = "删除就诊人")
    public Result<String> deleteMember(@PathVariable Long id, Authentication authentication) {
        Long patientId = (Long) authentication.getPrincipal();
        return patientService.deleteMember(patientId, id);
    }
}
