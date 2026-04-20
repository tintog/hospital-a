package com.hospital.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hospital.appointment.common.dto.ScheduleCreateReq;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.Schedule;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.mapper.DoctorMapper;
import com.hospital.appointment.mapper.ScheduleMapper;
import com.hospital.appointment.mapper.SlotMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private SlotMapper slotMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private SlotLockService slotLockService;

    public Result<List<Schedule>> listByDoctor(Long doctorId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, doctorId)
                .eq(Schedule::getStatus, 1);
        if (startDate != null) {
            wrapper.ge(Schedule::getWorkDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Schedule::getWorkDate, endDate);
        }
        wrapper.orderByAsc(Schedule::getWorkDate).orderByAsc(Schedule::getShiftType);
        return Result.success(scheduleMapper.selectList(wrapper));
    }

    public Result<List<Schedule>> listByDoctorName(String doctorName, LocalDate startDate, LocalDate endDate) {
        List<Long> doctorIds = doctorMapper.selectList(new LambdaQueryWrapper<Doctor>()
                        .like(Doctor::getName, doctorName)
                        .orderByAsc(Doctor::getId))
                .stream()
                .map(Doctor::getId)
                .collect(Collectors.toList());

        if (doctorIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<Schedule>()
                .in(Schedule::getDoctorId, doctorIds)
                .eq(Schedule::getStatus, 1);
        if (startDate != null) {
            wrapper.ge(Schedule::getWorkDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Schedule::getWorkDate, endDate);
        }
        wrapper.orderByAsc(Schedule::getWorkDate).orderByAsc(Schedule::getShiftType);

        return Result.success(scheduleMapper.selectList(wrapper));
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Schedule> create(ScheduleCreateReq req) {
        Schedule exists = scheduleMapper.selectOne(
                new LambdaQueryWrapper<Schedule>()
                        .eq(Schedule::getDoctorId, req.getDoctorId())
                        .eq(Schedule::getWorkDate, req.getWorkDate())
                        .eq(Schedule::getShiftType, req.getShiftType()));
        if (exists != null) {
            throw new BusinessException("该医生在此日期此班次已有排班");
        }

        Schedule schedule = new Schedule();
        schedule.setDoctorId(req.getDoctorId());
        schedule.setWorkDate(req.getWorkDate());
        schedule.setShiftType(req.getShiftType());
        schedule.setTotalSlots(req.getTotalSlots());
        schedule.setBookedSlots(0);
        schedule.setSlotDuration(req.getSlotDuration());
        schedule.setStartTime(req.getStartTime());
        schedule.setEndTime(req.getEndTime());
        schedule.setStatus(1);
        scheduleMapper.insert(schedule);

        generateSlots(schedule);

        return Result.success("排班创建成功", schedule);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> update(Long id, ScheduleCreateReq req) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("排班不存在");
        }
        schedule.setTotalSlots(req.getTotalSlots());
        schedule.setSlotDuration(req.getSlotDuration());
        schedule.setStartTime(req.getStartTime());
        schedule.setEndTime(req.getEndTime());
        scheduleMapper.updateById(schedule);
        return Result.success("更新成功", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelSchedule(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("排班不存在");
        }
        schedule.setStatus(0);
        scheduleMapper.updateById(schedule);

        slotMapper.update(null, new LambdaUpdateWrapper<Slot>()
                .eq(Slot::getScheduleId, id)
                .eq(Slot::getStatus, 0)
                .set(Slot::getStatus, 3));
        return Result.success("停诊成功", null);
    }

    private void generateSlots(Schedule schedule) {
        Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
        BigDecimal fee = calculateFee(doctor.getTitle());
        List<Slot> slots = new ArrayList<>();

        LocalTime currentTime = schedule.getStartTime();
        for (int i = 0; i < schedule.getTotalSlots(); i++) {
            LocalTime slotEnd = currentTime.plusMinutes(schedule.getSlotDuration());

            Slot slot = new Slot();
            slot.setScheduleId(schedule.getId());
            slot.setDoctorId(schedule.getDoctorId());
            slot.setDeptId(doctor.getDeptId());
            slot.setSlotNo("S" + schedule.getId() + "-" + String.format("%03d", i + 1));
            slot.setStartTime(LocalDateTime.of(schedule.getWorkDate(), currentTime));
            slot.setEndTime(LocalDateTime.of(schedule.getWorkDate(), slotEnd));
            slot.setFee(fee);
            slot.setStatus(0);
            slots.add(slot);

            currentTime = slotEnd;
        }

        for (Slot slot : slots) {
            slotMapper.insert(slot);
            slotLockService.initSlotInRedis(slot.getId());
        }
    }

    private BigDecimal calculateFee(String title) {
        if (title == null) return new BigDecimal("15.00");
        return switch (title) {
            case "主任医师" -> new BigDecimal("80.00");
            case "副主任医师" -> new BigDecimal("60.00");
            case "主治医师" -> new BigDecimal("30.00");
            default -> new BigDecimal("15.00");
        };
    }

    public Result<List<Schedule>> listAll(LocalDate date) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getStatus, 1);
        if (date != null) {
            wrapper.eq(Schedule::getWorkDate, date);
        }
        wrapper.orderByAsc(Schedule::getWorkDate);
        return Result.success(scheduleMapper.selectList(wrapper));
    }
}
