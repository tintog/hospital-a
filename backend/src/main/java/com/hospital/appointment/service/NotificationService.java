package com.hospital.appointment.service;

import com.hospital.appointment.entity.Appointment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendAppointmentSuccess(Appointment appointment) {
        log.info("[通知] 预约成功 - 订单号: {}, 患者ID: {}", appointment.getOrderNo(), appointment.getPatientId());
    }

    public void sendCancelNotification(Appointment appointment) {
        log.info("[通知] 预约已取消 - 订单号: {}, 患者ID: {}", appointment.getOrderNo(), appointment.getPatientId());
    }

    public void sendPaymentReminder(String orderNo) {
        log.info("[通知] 支付提醒 - 订单号: {}", orderNo);
    }
}
