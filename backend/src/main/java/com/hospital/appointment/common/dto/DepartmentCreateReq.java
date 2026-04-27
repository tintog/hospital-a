package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepartmentCreateReq {

    @NotBlank(message = "科室名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "排序不能为空")
    private Integer sortOrder;
}
