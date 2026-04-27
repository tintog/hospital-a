package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 手机号更新请求实体类
 * 使用@Data注解自动生成getter、setter、toString等方法
 */
@Data
public class PhoneUpdateReq {

    /**
     * 新手机号字段
     * 使用@NotBlank注解确保手机号不为空
     * 使用@Pattern注解验证手机号格式，必须是以1开头的11位数字
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String newPhone;
}
