package com.hospital.appointment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.common.dto.PasswordUpdateReq;
import com.hospital.appointment.common.dto.PhoneUpdateReq;
import com.hospital.appointment.common.dto.VisitStatusUpdateReq;
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

/**
 * 医生工作站控制器
 * 提供医生排班、患者管理、个人信息修改等功能接口
 */
@RestController
@RequestMapping("/doctor")
@Tag(name = "医生工作站接口")
public class DoctorWorkController {

    /**
     * 排班服务接口
     */
    @Autowired
    private ScheduleService scheduleService;

    /**
     * 预约服务接口
     */
    @Autowired
    private AppointmentService appointmentService;

    /**
     * 医生数据访问层
     */
    @Autowired
    private DoctorMapper doctorMapper;

    /**
     * 系统用户数据访问层
     */
    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 科室数据访问层
     */
    @Autowired
    private DepartmentMapper departmentMapper;

    /**
     * 密码编码器
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取医生排班信息
     * @param authentication 当前认证信息
     * @return 返回医生未来30天的排班信息
     */
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

    /**
     * 获取我的患者列表
     * @param page 页码，默认为1
     * @param size 每页大小，默认为20
     * @param authentication 当前认证信息
     * @return 返回分页的患者列表
     */
    @GetMapping("/today-patients") // HTTP GET请求映射到"/today-patients"路径
    @Operation(summary = "我的患者") // API操作描述，用于Swagger文档
    public Result<Page<AppointmentVO>> todayPatients( // 方法返回分页结果，包含预约视图对象列表
            @RequestParam(defaultValue = "1") int page, // 请求参数page，默认值为1
            @RequestParam(defaultValue = "20") int size, // 请求参数size，默认值为20
            @RequestParam(required = false, defaultValue = "false") Boolean history,
            Authentication authentication) { // 认证信息参数
        Long userId = (Long) authentication.getPrincipal(); // 从认证信息中获取用户ID
        Doctor doctor = doctorMapper.selectOne( // 根据用户ID查询医生信息
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) { // 如果医生信息不存在
            return Result.error(404, "未找到医生信息"); // 返回错误结果
        }
        return appointmentService.listByDoctor(doctor.getId(), history, page, size); // 调用服务方法获取预约列表
    }

    @PutMapping("/appointments/{id}/visit-status")
    @Operation(summary = "更新就诊状态")
    public Result<String> updateVisitStatus(@PathVariable Long id,
                                            @RequestBody VisitStatusUpdateReq req,
                                            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Doctor doctor = doctorMapper.selectOne(new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) {
            return Result.error(404, "未找到医生信息");
        }
        return appointmentService.updateVisitStatus(doctor.getId(), id, req.getVisitStatus());
    }

    /**
     * 获取医生个人信息
     * @param authentication 当前认证信息
     * @return 返回医生完整个人信息
     */
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

    /**
     * 修改医生登录账号（手机号）
     * @param req 包含新手机号的请求体
     * @param authentication 当前认证信息
     * @return 返回操作结果
     */
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

    /**
     * 修改医生密码
     * @param req 包含新旧密码的请求体
     * @param authentication 当前认证信息
     * @return 返回操作结果
     */
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
