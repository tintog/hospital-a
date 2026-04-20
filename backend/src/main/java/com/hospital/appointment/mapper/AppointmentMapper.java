package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    @Select("SELECT * FROM appointment WHERE order_no = #{orderNo} AND deleted = 0 LIMIT 1")
    Appointment selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT COUNT(*) FROM appointment WHERE patient_id = #{patientId} AND status = 2 AND cancel_by = 1 AND created_at >= #{since} AND deleted = 0")
    Integer countRecentCancellations(@Param("patientId") Long patientId, @Param("since") LocalDateTime since);
}
