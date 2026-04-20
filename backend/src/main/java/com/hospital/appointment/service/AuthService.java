package com.hospital.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.appointment.common.constant.RoleType;
import com.hospital.appointment.common.dto.LoginReq;
import com.hospital.appointment.common.dto.RealNameReq;
import com.hospital.appointment.common.dto.RegisterReq;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.common.utils.JwtUtil;
import com.hospital.appointment.common.vo.LoginVO;
import com.hospital.appointment.entity.Patient;
import com.hospital.appointment.entity.SysUser;
import com.hospital.appointment.mapper.PatientMapper;
import com.hospital.appointment.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Result<LoginVO> register(RegisterReq req) {
        Patient exists = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>().eq(Patient::getPhone, req.getPhone()));
        if (exists != null) {
            return Result.error(400, "该手机号已注册");
        }

        Patient patient = new Patient();
        patient.setPhone(req.getPhone());
        patient.setPassword(passwordEncoder.encode(req.getPassword()));
        patient.setAuthStatus(0);
        patientMapper.insert(patient);

        String token = jwtUtil.generateToken(patient.getId(), patient.getPhone(), "PATIENT");
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(patient.getId());
        vo.setUsername(patient.getPhone());
        vo.setRole("patient");
        return Result.success("注册成功", vo);
    }

    public Result<LoginVO> patientLogin(LoginReq req) {
        Patient patient = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>().eq(Patient::getPhone, req.getUsername()));
        if (patient == null) {
            return Result.error(400, "账号不存在");
        }
        if (!passwordEncoder.matches(req.getPassword(), patient.getPassword())) {
            return Result.error(400, "密码错误");
        }

        String token = jwtUtil.generateToken(patient.getId(), patient.getPhone(), "PATIENT");
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(patient.getId());
        vo.setUsername(patient.getPhone());
        vo.setRealName(patient.getRealName());
        vo.setRole("patient");
        return Result.success("登录成功", vo);
    }

    public Result<LoginVO> sysLogin(LoginReq req) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
        if (user == null) {
            return Result.error(400, "账号不存在");
        }
        if (user.getStatus() == 0) {
            return Result.error(400, "账号已被禁用");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return Result.error(400, "密码错误");
        }

        String role = RoleType.getRoleByCode(user.getRoleType());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(role.toLowerCase());
        return Result.success("登录成功", vo);
    }

    public Result<String> realNameAuth(Long patientId, RealNameReq req) {
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }
        patient.setRealName(req.getRealName());
        patient.setIdCard(req.getIdCard());
        patient.setAuthStatus(1);
        patientMapper.updateById(patient);
        return Result.success("实名认证成功", null);
    }
}
