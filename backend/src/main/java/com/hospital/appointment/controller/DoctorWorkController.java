package com.hospital.appointment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.common.dto.PasswordUpdateReq;
import com.hospital.appointment.common.dto.PhoneUpdateReq;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.common.vo.AppointmentVO;
import com.hospital.appointment.common.vo.DoctorProfileVO;
import com.hospital.appointment.entity.Department;
import com.hospital.appointment.entity.Schedule;
import com.hospital.appointment.entity.SysUser;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.mapper.DepartmentMapper;
import com.hospital.appointment.mapper.DoctorMapper;
import com.hospital.appointment.mapper.SysUserMapper;
import com.hospital.appointment.service.AppointmentService;
import com.hospital.appointment.service.ScheduleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @GetMapping("/profile")
    @Operation(summary = "医生个人信息")
    public Result<DoctorProfileVO> profile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Doctor doctor = doctorMapper.selectOne(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getUserId, userId));

        DoctorProfileVO vo = new DoctorProfileVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setDeptId(user.getDeptId());

        Department dept = user.getDeptId() == null ? null : departmentMapper.selectById(user.getDeptId());
        if (dept != null) {
            vo.setDeptName(dept.getName());
        }

        if (doctor != null) {
            vo.setDoctorId(doctor.getId());
            vo.setTitle(doctor.getTitle());
            vo.setSpecialty(doctor.getSpecialty());
        }

        return Result.success(vo);
    }

    @PutMapping("/profile/phone")
    @Operation(summary = "医生修改账户名（登录账号）")
    public Result<String> updatePhone(@RequestBody PhoneUpdateReq req, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        SysUser current = sysUserMapper.selectById(userId);
        if (current == null) {
            return Result.error(404, "用户不存在");
        }

        SysUser exists = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getNewPhone()));
        if (exists != null && !exists.getId().equals(userId)) {
            return Result.error(400, "该手机号已被使用");
        }

        current.setUsername(req.getNewPhone());
        sysUserMapper.updateById(current);
        return Result.success("账户名修改成功", null);
    }

    @PutMapping("/profile/password")
    @Operation(summary = "医生修改密码")
    public Result<String> updatePassword(@RequestBody PasswordUpdateReq req, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        SysUser current = sysUserMapper.selectById(userId);
        if (current == null) {
            return Result.error(404, "用户不存在");
        }
        if (!passwordEncoder.matches(req.getOldPassword(), current.getPassword())) {
            return Result.error(400, "旧密码错误");
        }
        if (req.getOldPassword().equals(req.getNewPassword())) {
            return Result.error(400, "新密码不能与旧密码相同");
        }

        current.setPassword(passwordEncoder.encode(req.getNewPassword()));
        sysUserMapper.updateById(current);
        return Result.success("密码修改成功，请重新登录", null);
    }
}
