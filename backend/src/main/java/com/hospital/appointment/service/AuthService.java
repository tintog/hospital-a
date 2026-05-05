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

/**
 * 用户注册方法
 * @param req 包含注册信息的请求对象，包含手机号和密码等
 * @return 返回一个Result对象，包含登录信息或错误信息
 */
    public Result<LoginVO> register(RegisterReq req) {
    // 查询数据库中是否已存在该手机号
        Patient exists = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>().eq(Patient::getPhone, req.getPhone()));
        if (exists != null) {
            return Result.error(400, "该手机号已注册");
        }

    // 创建新的Patient对象并设置属性
        Patient patient = new Patient();
        patient.setPhone(req.getPhone());
    // 对密码进行加密处理
        patient.setPassword(passwordEncoder.encode(req.getPassword()));
    // 设置认证状态为0（未认证）
        patient.setAuthStatus(0);
           patientMapper.insert(patient);

    // 生成JWT token
        String token = jwtUtil.generateToken(patient.getId(), patient.getPhone(), "PATIENT");
    // 创建登录视图对象并设置属性
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(patient.getId());
        vo.setUsername(patient.getPhone());
        vo.setRole("patient");
    // 返回注册成功信息和登录视图对象
        return Result.success("注册成功", vo);
    }

/**
 * 患者登录方法
 * @param req 登录请求参数，包含用户名和密码
 * @return 返回登录结果，包含token和用户信息
 */
    public Result<LoginVO> patientLogin(LoginReq req) {
    // 根据手机号查询患者信息
        Patient patient = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>().eq(Patient::getPhone, req.getUsername()));
    // 如果患者不存在，返回账号不存在的错误信息
        if (patient == null) {
            return Result.error(400, "账号不存在");
        }
    // 验证密码是否正确
        if (!passwordEncoder.matches(req.getPassword(), patient.getPassword())) {
            return Result.error(400, "密码错误");
        }

    // 生成JWT token
        String token = jwtUtil.generateToken(patient.getId(), patient.getPhone(), "PATIENT");
    // 构建登录返回对象
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(patient.getId());
        vo.setUsername(patient.getPhone());
        vo.setRealName(patient.getRealName());
        vo.setRole("patient");
    // 返回登录成功信息
        return Result.success("登录成功", vo);
    }

/**
 * 系统登录方法
 * @param req 登录请求参数，包含用户名和密码
 * @return 返回登录结果，包含token和用户信息
 */
    public Result<LoginVO> sysLogin(LoginReq req) {
    // 根据用户名查询用户信息
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
    // 判断用户是否存在
        if (user == null) {
            return Result.error(400, "账号不存在");
        }
    // 检查账号状态是否被禁用
        if (user.getStatus() == 0) {
            return Result.error(400, "账号已被禁用");
        }
    // 验证密码是否正确
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return Result.error(400, "密码错误");
        }

    // 获取用户角色
        String role = RoleType.getRoleByCode(user.getRoleType());
    // 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);
    // 构建登录成功返回对象
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(role.toLowerCase());
    // 返回登录成功结果
        return Result.success("登录成功", vo);
    }

/**
 * 患者实名认证方法
 * @param patientId 患者ID
 * @param req 实名认证请求参数，包含真实姓名和身份证号
 * @return 返回操作结果，成功时返回成功消息
 */
    public Result<String> realNameAuth(Long patientId, RealNameReq req) {
    // 根据患者ID查询患者信息
        Patient patient = patientMapper.selectById(patientId);
    // 如果患者不存在，抛出业务异常
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

    // 更新患者实名认证信息
        patient.setRealName(req.getRealName());
        patient.setIdCard(req.getIdCard());
        patient.setAuthStatus(1);
    // 更新患者信息到数据库
        patientMapper.updateById(patient);
        return Result.success("实名认证成功", null);
    }
}
