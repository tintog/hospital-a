package com.hospital.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.entity.Department;
import com.hospital.appointment.mapper.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    public Result<List<Department>> list() {
        List<Department> list = departmentMapper.selectList(
                new LambdaQueryWrapper<Department>()
                        .orderByAsc(Department::getSortOrder));
        return Result.success(list);
    }
}
