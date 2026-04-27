package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 密码更新请求类
 * 用于接收用户更新密码的请求参数
 * 使用了Lombok的@Data注解自动生成getter、setter等方法
 */
@Data
public class PasswordUpdateReq {

    /**
     * 旧密码字段
     * 不能为空，使用@NotBlank注解进行验证
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码字段
     * 不能为空，且长度需在6-20位之间
     * 使用@NotBlank和@Size注解进行验证
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度需在6-20位")
    private String newPassword;
}
