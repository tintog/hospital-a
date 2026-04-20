package com.hospital.appointment.common.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AppointmentVO {
    private Long id;
    private String orderNo;
    private Long slotId;
    private String slotNo;
    private LocalDateTime visitTime;
    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    private Long deptId;
    private String deptName;
    private BigDecimal fee;
    private Integer status;
    private String statusDesc;
    private String memberName;
    private LocalDateTime payTime;
    private String cancelReason;
    private LocalDateTime createdAt;
}
