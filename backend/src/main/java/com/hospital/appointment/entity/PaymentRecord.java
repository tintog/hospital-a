package com.hospital.appointment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String transactionId;
    private BigDecimal amount;
    private Integer status;
    private String payChannel;
    private LocalDateTime callbackTime;
    private LocalDateTime refundTime;
    private LocalDateTime createdAt;
}
