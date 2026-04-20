package com.hospital.appointment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("patient_member")
public class PatientMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private String name;
    private String idCard;
    private String relation;
    private Integer authStatus;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
}
