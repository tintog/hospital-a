package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医生数据访问接口
 * 继承自BaseMapper，提供Doctor实体的基础数据库操作方法
 * 使用@Mapper注解标记为MyBatis的Mapper接口
 */
@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
}
