package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志数据访问接口
 * 继承MyBatis-Plus的基础Mapper接口，提供OperationLog实体的基本CRUD操作
 * 使用@Mapper注解标记为MyBatis的映射接口
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    // 接口体为空，因为继承了BaseMapper的所有基本方法
    // BaseMapper已经提供了通用的增删改查方法，无需额外定义
}
