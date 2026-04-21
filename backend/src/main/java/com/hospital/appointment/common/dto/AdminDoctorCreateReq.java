package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminDoctorCreateReq {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20位")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotNull(message = "科室不能为空")
    private Long deptId;

    @NotBlank(message = "职称不能为空")
    private String title;

    @NotBlank(message = "擅长不能为空")
    private String specialty;

    private String introduction;
}
