package com.hospital.appointment.service;

import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.entity.PaymentRecord;
import com.hospital.appointment.mapper.PaymentRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;

    public void createPaymentRecord(String orderNo, BigDecimal amount) {
        PaymentRecord record = new PaymentRecord();
        record.setOrderNo(orderNo);
        record.setAmount(amount);
        record.setStatus(0);
        record.setPayChannel("SANDBOX");
        paymentRecordMapper.insert(record);
    }

    public Result<String> confirmPayment(String orderNo, String transactionId) {
        PaymentRecord record = paymentRecordMapper.selectByOrderNo(orderNo);
        if (record == null) {
            return Result.error(404, "支付记录不存在");
        }
        record.setStatus(1);
        record.setTransactionId(transactionId);
        record.setCallbackTime(LocalDateTime.now());
        paymentRecordMapper.updateById(record);
        return Result.success("支付确认成功", null);
    }

    public void refund(String orderNo) {
        PaymentRecord record = paymentRecordMapper.selectByOrderNo(orderNo);
        if (record != null && record.getStatus() == 1) {
            record.setStatus(3);
            record.setRefundTime(LocalDateTime.now());
            paymentRecordMapper.updateById(record);
            log.info("订单{}已退款", orderNo);
        }
    }
}
