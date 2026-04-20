package com.hospital.appointment.common.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StatisticsVO {
    private Long totalAppointments;
    private Long todayAppointments;
    private Long totalPatients;
    private Long totalDoctors;
    private List<Map<String, Object>> deptDistribution;
    private List<Map<String, Object>> weeklyTrend;
}
