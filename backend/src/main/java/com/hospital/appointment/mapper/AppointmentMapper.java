package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

/**
 * 根据订单号查询预约信息
 * @param orderNo 订单号，用于查询对应的预约记录
 * @return 返回查找到的预约对象，如果没有找到则返回null
 */
    @Select("SELECT * FROM appointment WHERE order_no = #{orderNo} AND deleted = 0 LIMIT 1")
    Appointment selectByOrderNo(@Param("orderNo") String orderNo);

/**
 * 统计患者最近取消的预约数量
 *
 * @param patientId 患者ID
 * @param since 统计起始时间点
 * @return 返回符合条件的预约记录数量
 */
    @Select("SELECT COUNT(*) FROM appointment WHERE patient_id = #{patientId} AND status = 2 AND cancel_by = 1 AND created_at >= #{since} AND deleted = 0")
    Integer countRecentCancellations(@Param("patientId") Long patientId, @Param("since") LocalDateTime since);
}
