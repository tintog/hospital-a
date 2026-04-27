package com.hospital.appointment.common.vo;

import lombok.Data;

/**
 * 登录结果值对象（Value Object）
 * 用于封装用户登录成功后返回的信息
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class LoginVO {
    private String token;        // JWT认证令牌，用于后续请求的身份认证
    private Long userId;         // 用户ID，唯一标识用户
    private String username;     // 用户名，用于登录的账号
    private String realName;     // 真实姓名，用户的真实姓名信息
    private String role;         // 用户角色，标识用户在系统中的权限级别
}
