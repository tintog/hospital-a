package com.hospital.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.common.constant.RoleType;
import com.hospital.appointment.common.dto.AdminBlacklistReq;
import com.hospital.appointment.common.dto.AdminDoctorCreateReq;
import com.hospital.appointment.common.dto.AdminDoctorUpdateReq;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.common.vo.AdminDoctorVO;
import com.hospital.appointment.common.vo.AdminPatientVO;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.Department;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.Patient;
import com.hospital.appointment.entity.SysUser;
import com.hospital.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.mapper.DepartmentMapper;
import com.hospital.appointment.mapper.DoctorMapper;
import com.hospital.appointment.mapper.PatientMapper;
import com.hospital.appointment.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminManageService {

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Result<Page<AdminPatientVO>> listPatients(String phone, String realName, Integer authStatus, Integer blacklistStatus, int page, int size) {
        Page<Patient> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(phone)) {
            wrapper.like(Patient::getPhone, phone);
        }
        if (StringUtils.hasText(realName)) {
            wrapper.like(Patient::getRealName, realName);
        }
        if (authStatus != null) {
            wrapper.eq(Patient::getAuthStatus, authStatus);
        }
        if (blacklistStatus != null) {
            LocalDateTime now = LocalDateTime.now();
            if (blacklistStatus == 1) {
                wrapper.isNotNull(Patient::getBlacklistEndTime).gt(Patient::getBlacklistEndTime, now);
            } else if (blacklistStatus == 0) {
                wrapper.and(w -> w.isNull(Patient::getBlacklistEndTime).or().le(Patient::getBlacklistEndTime, now));
            }
        }
        wrapper.orderByDesc(Patient::getCreatedAt);

        Page<Patient> result = patientMapper.selectPage(pageParam, wrapper);
        Page<AdminPatientVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toPatientVO).collect(Collectors.toList()));
        return Result.success(voPage);
    }

    public Result<AdminPatientVO> patientDetail(Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        return Result.success(toPatientVO(patient));
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> blacklistPatient(Long id, AdminBlacklistReq req) {
        Patient patient = patientMapper.selectById(id);
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        patient.setBlacklistEndTime(LocalDateTime.now().plusDays(req.getDays()));
        patientMapper.updateById(patient);
        return Result.success("拉黑成功", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> unblacklistPatient(Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        patient.setBlacklistEndTime(null);
        patientMapper.updateById(patient);
        return Result.success("已解除黑名单", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> deletePatient(Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        patientMapper.deleteById(id);
        return Result.success("患者已删除", null);
    }

    public Result<Page<AdminDoctorVO>> listDoctors(String name, Long deptId, String title, Integer status, int page, int size) {
        Page<Doctor> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Doctor::getName, name);
        }
        if (deptId != null) {
            wrapper.eq(Doctor::getDeptId, deptId);
        }
        if (StringUtils.hasText(title)) {
            wrapper.eq(Doctor::getTitle, title);
        }
        wrapper.orderByAsc(Doctor::getId);

        Page<Doctor> result = doctorMapper.selectPage(pageParam, wrapper);
        List<AdminDoctorVO> records = result.getRecords().stream()
                .map(this::toDoctorVO)
                .filter(vo -> status == null || status.equals(vo.getStatus()))
                .collect(Collectors.toList());

        Page<AdminDoctorVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(records);
        return Result.success(voPage);
    }

    public Result<AdminDoctorVO> doctorDetail(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            throw new BusinessException("医生不存在");
        }
        return Result.success(toDoctorVO(doctor));
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<AdminDoctorVO> createDoctor(AdminDoctorCreateReq req) {
        SysUser userExists = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername()));
        if (userExists != null) {
            throw new BusinessException("账号已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRealName(req.getName());
        user.setRoleType(RoleType.DOCTOR.getCode());
        user.setDeptId(req.getDeptId());
        user.setStatus(1);
        sysUserMapper.insert(user);

        Doctor doctor = new Doctor();
        doctor.setUserId(user.getId());
        doctor.setName(req.getName());
        doctor.setDeptId(req.getDeptId());
        doctor.setTitle(req.getTitle());
        doctor.setSpecialty(req.getSpecialty());
        doctor.setIntroduction(req.getIntroduction());
        doctorMapper.insert(doctor);

        return Result.success("医生创建成功", toDoctorVO(doctor));
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateDoctor(Long id, AdminDoctorUpdateReq req) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            throw new BusinessException("医生不存在");
        }
        doctor.setName(req.getName());
        doctor.setDeptId(req.getDeptId());
        doctor.setTitle(req.getTitle());
        doctor.setSpecialty(req.getSpecialty());
        doctor.setIntroduction(req.getIntroduction());
        doctorMapper.updateById(doctor);

        if (doctor.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(doctor.getUserId());
            if (user != null) {
                user.setRealName(req.getName());
                user.setDeptId(req.getDeptId());
                user.setStatus(req.getStatus());
                sysUserMapper.updateById(user);
            }
        }

        return Result.success("医生信息更新成功", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteDoctor(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            throw new BusinessException("医生不存在");
        }
        doctorMapper.deleteById(id);
        if (doctor.getUserId() != null) {
            sysUserMapper.deleteById(doctor.getUserId());
        }
        return Result.success("医生已删除", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> backfillDoctorAccounts() {
        List<Doctor> doctors = doctorMapper.selectList(new LambdaQueryWrapper<Doctor>());
        int created = 0;

        for (Doctor doctor : doctors) {
            if (doctor.getUserId() != null) {
                SysUser user = sysUserMapper.selectById(doctor.getUserId());
                if (user != null) {
                    continue;
                }
            }

            String username = generateUniqueDoctorUsername(doctor.getName(), doctor.getId());
            SysUser user = new SysUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRealName(doctor.getName());
            user.setRoleType(RoleType.DOCTOR.getCode());
            user.setDeptId(doctor.getDeptId());
            user.setStatus(1);
            sysUserMapper.insert(user);

            doctor.setUserId(user.getId());
            doctorMapper.updateById(doctor);
            created++;
        }

        return Result.success("补齐完成，新增医生账号" + created + "个，初始密码均为123456", null);
    }

    private AdminPatientVO toPatientVO(Patient patient) {
        AdminPatientVO vo = new AdminPatientVO();
        vo.setId(patient.getId());
        vo.setPhone(patient.getPhone());
        vo.setRealName(patient.getRealName());
        vo.setAuthStatus(patient.getAuthStatus());
        vo.setBlacklistEndTime(patient.getBlacklistEndTime());
        vo.setCreatedAt(patient.getCreatedAt());

        Long count = appointmentMapper.selectCount(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getPatientId, patient.getId()));
        vo.setRecentAppointmentCount(count == null ? 0L : count);
        return vo;
    }

    private String generateUniqueDoctorUsername(String name, Long doctorId) {
        String base = "doctor_" + toPinyinLike(name);
        List<String> candidates = new ArrayList<>();
        candidates.add(base);
        candidates.add(base + doctorId);
        candidates.add("doctor_" + doctorId);

        for (String candidate : candidates) {
            SysUser exists = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, candidate));
            if (exists == null) {
                return candidate;
            }
        }

        return "doctor_" + doctorId + "_" + System.currentTimeMillis();
    }

    private String toPinyinLike(String name) {
        if (!StringUtils.hasText(name)) {
            return "user";
        }
        String normalized = name.replaceAll("\\s+", "");
        if ("张伟".equals(normalized)) return "zhang";
        if ("李芳".equals(normalized)) return "li";
        if ("王强".equals(normalized)) return "wang";
        if ("赵敏".equals(normalized)) return "zhao";
        if ("刘洋".equals(normalized)) return "liu";
        if ("陈静".equals(normalized)) return "chen";
        if ("孙丽".equals(normalized)) return "sun";
        if ("周明".equals(normalized)) return "zhou";

        return "user";
    }

    private AdminDoctorVO toDoctorVO(Doctor doctor) {
        AdminDoctorVO vo = new AdminDoctorVO();
        vo.setId(doctor.getId());
        vo.setUserId(doctor.getUserId());
        vo.setName(doctor.getName());
        vo.setDeptId(doctor.getDeptId());
        vo.setTitle(doctor.getTitle());
        vo.setSpecialty(doctor.getSpecialty());
        vo.setIntroduction(doctor.getIntroduction());

        Department dept = doctor.getDeptId() == null ? null : departmentMapper.selectById(doctor.getDeptId());
        if (dept != null) {
            vo.setDeptName(dept.getName());
        }

        if (doctor.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(doctor.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setStatus(user.getStatus());
            }
        }
        return vo;
    }
}
