package com.hospital.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.common.vo.StatisticsVO;
import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.Department;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.Patient;
import com.hospital.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.mapper.DepartmentMapper;
import com.hospital.appointment.mapper.DoctorMapper;
import com.hospital.appointment.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private DepartmentMapper departmentMapper;

    public Result<StatisticsVO> getDashboardStats() {
        StatisticsVO vo = new StatisticsVO();

        vo.setTotalAppointments(appointmentMapper.selectCount(null));
        vo.setTodayAppointments(appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>()
                        .ge(Appointment::getCreatedAt, LocalDate.now().atStartOfDay())
                        .lt(Appointment::getCreatedAt, LocalDate.now().plusDays(1).atStartOfDay())));
        vo.setTotalPatients(patientMapper.selectCount(null));
        vo.setTotalDoctors(doctorMapper.selectCount(null));

        List<Department> depts = departmentMapper.selectList(null);
        List<Map<String, Object>> deptDist = new ArrayList<>();
        for (Department dept : depts) {
            long count = appointmentMapper.selectCount(
                    new LambdaQueryWrapper<Appointment>().eq(Appointment::getDeptId, dept.getId()));
            Map<String, Object> item = new HashMap<>();
            item.put("name", dept.getName());
            item.put("value", count);
            deptDist.add(item);
        }
        vo.setDeptDistribution(deptDist);

        List<Map<String, Object>> weeklyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long count = appointmentMapper.selectCount(
                    new LambdaQueryWrapper<Appointment>()
                            .ge(Appointment::getCreatedAt, date.atStartOfDay())
                            .lt(Appointment::getCreatedAt, date.plusDays(1).atStartOfDay()));
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", count);
            weeklyTrend.add(item);
        }
        vo.setWeeklyTrend(weeklyTrend);

        return Result.success(vo);
    }
}
