package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import lombok.Data;
/**
 * 实名认证请求类
 * 用于接收用户实名认证的请求参数
 */
@Data
public class RealNameReq {

    @NotBlank(message = "姓名不能为空")
    private String realName;

    /**
     * 用户身份证号码
     * 使用@NotBlank注解确保该字段不能为空
     * 使用@Pattern注解验证身份证号码格式，必须为17位数字加1位数字或X(不区分大小写)
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    private String idCard;
}
