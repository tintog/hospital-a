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

/**
 * 根据科室ID和职称查询医生列表
 * @param deptId 科室ID，可为null
 * @param title 职称，可为null或空字符串
 * @return 返回医生列表的Result对象，包含查询到的医生列表
 */
    public Result<List<Doctor>> list(Long deptId, String title) {
    // 创建Lambda查询包装器
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
    // 如果deptId不为null，添加科室ID等于deptId的条件
        if (deptId != null) {
            wrapper.eq(Doctor::getDeptId, deptId);
        }
    // 如果title不为空，添加职称等于title的条件
        if (StringUtils.hasText(title)) {
            wrapper.eq(Doctor::getTitle, title);
        }
    // 按ID升序排序
        wrapper.orderByAsc(Doctor::getId);
    // 执行查询并获取结果列表
        List<Doctor> list = doctorMapper.selectList(wrapper);
    // 返回成功结果，包含查询到的医生列表
        return Result.success(list);
    }

/**
 * 根据医生ID查询医生详细信息
 * @param id 医生ID
 * @return 返回Result对象，包含医生信息或错误信息
 */
    public Result<Doctor> detail(Long id) {
    // 根据ID查询医生信息
        Doctor doctor = doctorMapper.selectById(id);
    // 判断医生是否存在，不存在则返回错误信息
        if (doctor == null) {
            return Result.error(404, "医生不存在");
        }
    // 返回查询成功的医生信息
        return Result.success(doctor);
    }
}
