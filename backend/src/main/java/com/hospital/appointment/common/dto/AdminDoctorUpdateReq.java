package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminDoctorUpdateReq {

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotNull(message = "科室不能为空")
    private Long deptId;

    @NotBlank(message = "职称不能为空")
    private String title;

    @NotBlank(message = "擅长不能为空")
    private String specialty;

    private String introduction;

    @NotNull(message = "账号状态不能为空")
    private Integer status;
}
