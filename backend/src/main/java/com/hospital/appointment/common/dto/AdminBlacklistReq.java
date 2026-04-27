package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员拉黑请求类
 * 用于接收和管理员拉黑相关的请求参数
 */
@Data  // 使用Lombok的@Data注解，自动生成getter、setter等方法
public class AdminBlacklistReq {



    /**
     * 拉黑天数
     * 该字段不能为空，最小值为1天，最大值为365天
     */
    @NotNull(message = "拉黑天数不能为空")  // 验证注解：字段不能为null
    @Min(value = 1, message = "拉黑天数最少1天")  // 验证注解：字段最小值为1
    @Max(value = 365, message = "拉黑天数最多365天")  // 验证注解：字段最大值为365
    private Integer days;  // 拉黑天数的整数值
}
