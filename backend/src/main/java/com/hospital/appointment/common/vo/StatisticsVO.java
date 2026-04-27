package com.hospital.appointment.common.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 统计数据值对象(VO)，用于封装和展示各类统计信息
 * 包含预约总数、今日预约数、患者总数、医生总数等统计数据
 * 以及部门分布和周趋势等复杂数据结构
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class StatisticsVO {
    private Long totalAppointments;    // 预约总数统计
    private Long todayAppointments;    // 今日预约数统计
    private Long totalPatients;        // 患者总数统计
    private Long totalDoctors;         // 医生总数统计
    private List<Map<String, Object>> deptDistribution;  // 部门分布数据列表，使用Map存储各部门的统计数据
    private List<Map<String, Object>> weeklyTrend;       // 周趋势数据列表，使用Map存储每周的统计数据
}
