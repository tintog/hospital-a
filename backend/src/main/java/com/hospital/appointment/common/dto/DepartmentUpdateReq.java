package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 科室更新请求实体类
 * 用于接收前端传递的科室更新数据
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class DepartmentUpdateReq {

    @NotBlank(message = "科室名称不能为空")  // 验证注解，确保name字段不为空
    private String name;  // 科室名称

    private String description;  // 科室描述信息

    @NotNull(message = "排序不能为空")  // 验证注解，确保sortOrder字段不为null
    private Integer sortOrder;  // 科室排序值
}
