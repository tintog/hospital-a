package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {

/**
 * 根据订单号查询支付记录
 * @param orderNo 订单号，用于查询对应的支付记录
 * @return 返回匹配指定订单号的支付记录，如果未找到则返回null
 */
    @Select("SELECT * FROM payment_record WHERE order_no = #{orderNo} LIMIT 1")
    PaymentRecord selectByOrderNo(@Param("orderNo") String orderNo);
}
