package com.hospital.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.appointment.common.dto.DepartmentCreateReq;
import com.hospital.appointment.common.dto.DepartmentUpdateReq;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.entity.Department;
import com.hospital.appointment.mapper.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 科室服务类
 * 提供科室相关的业务逻辑处理，包括科室列表查询、科室创建、科室更新等功能
 */
@Service
public class DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    /**
     * 获取科室列表
     * 按照排序顺序和ID升序排列所有科室
     * @return 返回科室列表结果
     */
    public Result<List<Department>> list() {
        List<Department> list = departmentMapper.selectList(
                new LambdaQueryWrapper<Department>()
                        .orderByAsc(Department::getSortOrder)  // 按排序顺序升序
                        .orderByAsc(Department::getId));       // 按ID升序
        return Result.success(list);
    }

/**
 * 创建新科室的方法
 * @param req 包含科室创建信息的请求对象
 * @return 返回操作结果，包含创建成功的科室信息
 */
    public Result<Department> create(DepartmentCreateReq req) {
    // 获取并去除科室名称前后的空格
        String name = req.getName().trim();
    // 查询数据库中是否已存在相同名称的科室
        Department exists = departmentMapper.selectOne(
                new LambdaQueryWrapper<Department>().eq(Department::getName, name));
    // 如果科室已存在，则抛出业务异常
        if (exists != null) {
            throw new BusinessException("科室名称已存在");
        }

    // 创建新的科室对象
        Department department = new Department();
    // 设置科室名称
        department.setName(name);
    // 设置科室描述，如果描述为空则设置为null
        department.setDescription(StringUtils.hasText(req.getDescription()) ? req.getDescription().trim() : null);
    // 设置科室排序顺序
        department.setSortOrder(req.getSortOrder());
    // 将新科室插入数据库
        departmentMapper.insert(department);
    // 返回成功结果，包含创建的科室信息
        return Result.success("新增成功", department);
    }

    public Result<Department> update(Long id, DepartmentUpdateReq req) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException("科室不存在");
        }

        String name = req.getName().trim();
        Department exists = departmentMapper.selectOne(new LambdaQueryWrapper<Department>()
                .eq(Department::getName, name)
                .ne(Department::getId, id));
        if (exists != null) {
            throw new BusinessException("科室名称已存在");
        }

        department.setName(name);
        department.setDescription(StringUtils.hasText(req.getDescription()) ? req.getDescription().trim() : null);
        department.setSortOrder(req.getSortOrder());
        departmentMapper.updateById(department);
        return Result.success("更新成功", department);
    }
}

