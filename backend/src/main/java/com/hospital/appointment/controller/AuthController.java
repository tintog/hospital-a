package com.hospital.appointment.controller;

import com.hospital.appointment.common.dto.LoginReq;
import com.hospital.appointment.common.dto.RealNameReq;
import com.hospital.appointment.common.dto.RegisterReq;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.common.vo.LoginVO;
import com.hospital.appointment.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "患者注册")
    public Result<LoginVO> register(@Valid @RequestBody RegisterReq req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    @Operation(summary = "患者登录")
    public Result<LoginVO> patientLogin(@Valid @RequestBody LoginReq req) {
        return authService.patientLogin(req);
    }

    @PostMapping("/sys-login")
    @Operation(summary = "系统用户登录（管理员/医生）")
    public Result<LoginVO> sysLogin(@Valid @RequestBody LoginReq req) {
        return authService.sysLogin(req);
    }

    @PostMapping("/realname")
    @Operation(summary = "实名认证")
    public Result<String> realNameAuth(@Valid @RequestBody RealNameReq req, Authentication authentication) {
        Long patientId = (Long) authentication.getPrincipal();
        return authService.realNameAuth(patientId, req);
    }
}
