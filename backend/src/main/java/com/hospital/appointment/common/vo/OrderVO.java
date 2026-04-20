package com.hospital.appointment.common.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderVO {
    private String orderNo;
    private Long slotId;
    private BigDecimal fee;
    private Integer expireSeconds;
    private String qrCodeUrl;
}
