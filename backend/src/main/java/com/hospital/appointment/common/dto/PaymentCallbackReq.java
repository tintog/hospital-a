package com.hospital.appointment.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentCallbackReq {
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    private String transactionId;
}
