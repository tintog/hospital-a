package com.hospital.appointment.common.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约视图对象（Value Object）
 * 用于封装预约相关的数据，用于前端展示或数据传输
 */
@Data  // 使用Lombok的@Data注解，自动生成getter、setter、toString等方法
public class AppointmentVO {
    private Long id;                    // 预约ID
    private String orderNo;             // 预约订单号
    private Long slotId;                // 预约时间段ID
    private String slotNo;              // 预约时间段编号
    private LocalDateTime visitTime;    // 预约到院时间
    private Long doctorId;              // 医生ID
    private String doctorName;          // 医生姓名
    private String doctorTitle;         // 医生职称
    private Long deptId;                // 科室ID
    private String deptName;            // 科室名称
    private BigDecimal fee;             // 预约费用
    private Integer status;             // 预约状态
    private String statusDesc;          // 预约状态描述
    private String memberName;          // 预约人姓名
    private Integer visitStatus;        // 就诊状态：0未就诊，1已就诊
    private String visitStatusDesc;     // 就诊状态描述
    private LocalDateTime payTime;      // 支付时间
    private String cancelReason;        // 取消原因
    private LocalDateTime createdAt;    // 创建时间
}
