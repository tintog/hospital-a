package com.hospital.appointment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.common.vo.AppointmentVO;
import com.hospital.appointment.entity.Schedule;
import com.hospital.appointment.entity.SysUser;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.mapper.DoctorMapper;
import com.hospital.appointment.mapper.SysUserMapper;
import com.hospital.appointment.service.AppointmentService;
import com.hospital.appointment.service.ScheduleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/doctor")
@Tag(name = "医生工作站接口")
public class DoctorWorkController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorMapper doctorMapper;

    @GetMapping("/my-schedule")
    @Operation(summary = "我的排班")
    public Result<List<Schedule>> mySchedule(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) {
            return Result.error(404, "未找到医生信息");
        }
        return scheduleService.listByDoctor(doctor.getId(), LocalDate.now(), LocalDate.now().plusDays(30));
    }

    @GetMapping("/today-patients")
    @Operation(summary = "今日患者")
    public Result<Page<AppointmentVO>> todayPatients(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) {
            return Result.error(404, "未找到医生信息");
        }
        return appointmentService.listAll(null, doctor.getId(), page, size);
    }
}
