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

/**
 * 根据医生ID和日期获取医生的排班信息
 * @param doctorId 医生ID
 * @param date 日期字符串，格式应为YYYY-MM-DD
 * @return 返回包含排班信息的Result对象，数据类型为SlotVO列表
 */
    public Result<List<SlotVO>> getSlotsByDoctorAndDate(Long doctorId, String date) {
    // 将输入的日期字符串转换为LocalDate对象
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime dayStart = localDate.atStartOfDay();
        LocalDateTime dayEnd = localDate.plusDays(1).atStartOfDay();

    // 从数据库查询指定医生在指定日期的所有排班记录
        List<Slot> slots = slotMapper.selectList(
                new LambdaQueryWrapper<Slot>()
                        .eq(Slot::getDoctorId, doctorId)
                    // 筛选条件：开始时间大于等于当天开始时间
                        .ge(Slot::getStartTime, dayStart)
                    // 筛选条件：开始时间小于第二天开始时间
                        .lt(Slot::getStartTime, dayEnd)
                    // 按开始时间升序排序
                        .orderByAsc(Slot::getStartTime));

    // 根据医生ID查询医生信息
        Doctor doctor = doctorMapper.selectById(doctorId);
    // 如果医生存在，则查询其所属科室信息
        Department dept = doctor != null ? departmentMapper.selectById(doctor.getDeptId()) : null;

    // 将Slot对象列表转换为SlotVO对象列表
        List<SlotVO> voList = slots.stream().map(slot -> {
        // 创建SlotVO对象并复制基本属性
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
