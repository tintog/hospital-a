package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 注册请求实体类
 * 用于接收用户注册时的相关信息
 * 使用@Data注解自动生成getter、setter等方法*/
@Data
public class RegisterReq {
    /** 不能为空，且必须符合中国手机号格式规则
     * 规则：以1开头，第二位为3-9之间的数字，后面跟着9位数字*/
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 密码字段 不能为空*/
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 短信验证码字段
     * 可选字段，用户注册时可能需要提供的短信验证码*/
    private String smsCode;
}
