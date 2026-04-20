package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.PatientMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PatientMemberMapper extends BaseMapper<PatientMember> {
}
