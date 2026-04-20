package com.hospital.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.mapper.DoctorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    public Result<List<Doctor>> list(Long deptId, String title) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        if (deptId != null) {
            wrapper.eq(Doctor::getDeptId, deptId);
        }
        if (StringUtils.hasText(title)) {
            wrapper.eq(Doctor::getTitle, title);
        }
        wrapper.orderByAsc(Doctor::getId);
        List<Doctor> list = doctorMapper.selectList(wrapper);
        return Result.success(list);
    }

    public Result<Doctor> detail(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            return Result.error(404, "医生不存在");
        }
        return Result.success(doctor);
    }
}
