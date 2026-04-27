package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 科室数据访问接口
 * 继承自MyBatis-Plus的BaseMapper，提供基础的CRUD操作
 * 使用@Mapper注解标记为MyBatis的Mapper接口
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    // 该接口继承BaseMapper<Department>，已经包含了Department实体的基本数据库操作方法
    // 如：插入、删除、更新、查询等，无需额外定义方法
}
