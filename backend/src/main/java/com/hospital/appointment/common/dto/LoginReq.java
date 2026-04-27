package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import lombok.Data;
/**
 * 登录请求类
 * 用于接收前端传递的登录参数，包含账号、密码和角色信息
 */
@Data
public class LoginReq {
    /**
     * 用户名
     * 不能为空，用于验证用户身份
     */
    @NotBlank(message = "账号不能为空")
    private String username;
    /**
     * 密码
     * 不能为空，用于验证用户身份
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    /**
     * 角色
     * 可选参数，用于标识用户角色
     */
    private String role;
}
