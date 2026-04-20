package com.hospital.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.common.vo.SlotVO;
import com.hospital.appointment.entity.Department;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.mapper.DepartmentMapper;
import com.hospital.appointment.mapper.DoctorMapper;
import com.hospital.appointment.mapper.SlotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlotService {

    @Autowired
    private SlotMapper slotMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    public Result<List<SlotVO>> getSlotsByDoctorAndDate(Long doctorId, String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime dayStart = localDate.atStartOfDay();
        LocalDateTime dayEnd = localDate.plusDays(1).atStartOfDay();

        List<Slot> slots = slotMapper.selectList(
                new LambdaQueryWrapper<Slot>()
                        .eq(Slot::getDoctorId, doctorId)
                        .ge(Slot::getStartTime, dayStart)
                        .lt(Slot::getStartTime, dayEnd)
                        .orderByAsc(Slot::getStartTime));

        Doctor doctor = doctorMapper.selectById(doctorId);
        Department dept = doctor != null ? departmentMapper.selectById(doctor.getDeptId()) : null;

        List<SlotVO> voList = slots.stream().map(slot -> {
            SlotVO vo = new SlotVO();
            vo.setId(slot.getId());
            vo.setSlotNo(slot.getSlotNo());
            vo.setStartTime(slot.getStartTime());
            vo.setEndTime(slot.getEndTime());
            vo.setFee(slot.getFee());
            vo.setStatus(slot.getStatus());
            if (doctor != null) {
                vo.setDoctorName(doctor.getName());
                vo.setTitle(doctor.getTitle());
            }
            if (dept != null) {
                vo.setDeptName(dept.getName());
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    public Result<String> revokeSlot(Long slotId) {
        Slot slot = slotMapper.selectById(slotId);
        if (slot == null) {
            return Result.error(404, "号源不存在");
        }
        if (slot.getStatus() != 0) {
            return Result.error(400, "只能回撤可预约状态的号源");
        }
        slotMapper.updateStatus(slotId, 3);
        return Result.success("号源已回撤", null);
    }
}
