package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {

    @Select("SELECT * FROM payment_record WHERE order_no = #{orderNo} LIMIT 1")
    PaymentRecord selectByOrderNo(@Param("orderNo") String orderNo);
}
