package com.hospital.appointment.common.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SlotVO {
    private Long id;
    private String slotNo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal fee;
    private Integer status;
    private String doctorName;
    private String title;
    private String deptName;
}
