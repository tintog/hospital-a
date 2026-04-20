package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface PatientMapper extends BaseMapper<Patient> {

    @Update("UPDATE patient SET blacklist_end_time = #{endTime} WHERE id = #{patientId}")
    int updateBlacklistEndTime(@Param("patientId") Long patientId, @Param("endTime") LocalDateTime endTime);
}
