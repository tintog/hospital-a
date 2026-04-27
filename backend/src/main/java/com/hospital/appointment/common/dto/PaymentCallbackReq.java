package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 支付回调请求实体类
 * 用于接收支付平台回调的支付结果信息
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class PaymentCallbackReq {



    @NotBlank(message = "订单号不能为空")  // 校验注解，确保该字段不为空
    private String orderNo;
    private String transactionId;
}
