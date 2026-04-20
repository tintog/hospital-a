package com.hospital.appointment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.common.constant.AppointmentStatus;
import com.hospital.appointment.common.constant.SlotStatus;
import com.hospital.appointment.common.dto.AppointmentCreateReq;
import com.hospital.appointment.common.dto.PaymentCallbackReq;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.common.vo.AppointmentVO;
import com.hospital.appointment.common.vo.OrderVO;
import com.hospital.appointment.entity.*;
import com.hospital.appointment.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AppointmentService {

    @Autowired
    private SlotLockService slotLockService;
    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private SlotMapper slotMapper;
    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private PatientMemberMapper memberMapper;
    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private NotificationService notificationService;

    @Value("${appointment.blacklist-threshold:3}")
    private Integer blacklistThreshold;

    @Value("${appointment.blacklist-days:30}")
    private Integer blacklistDays;

    @Transactional(rollbackFor = Exception.class)
    public Result<OrderVO> createAppointment(AppointmentCreateReq req, Long patientId) {
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            return Result.error(400, "患者不存在");
        }
        if (patient.getBlacklistEndTime() != null &&
                patient.getBlacklistEndTime().isAfter(LocalDateTime.now())) {
            return Result.error(403, "您已被限制预约，解除时间: " + patient.getBlacklistEndTime());
        }

        Slot slot = slotMapper.selectById(req.getSlotId());
        if (slot == null) {
            return Result.error(400, "号源不存在");
        }

        // Try Redis lock first, fallback to DB lock if Redis is unavailable
        try {
            Long lockResult = slotLockService.lockSlot(req.getSlotId(), patientId);
            if (lockResult == -1L) {
                return Result.error(400, "该号源不可预约（可能已约满或停诊）");
            } else if (lockResult == 0L) {
                return Result.error(409, "号源已被他人锁定，请刷新重试");
            }
        } catch (Exception e) {
            log.warn("Redis锁不可用，降级为数据库锁: {}", e.getMessage());
            if (slot.getStatus() != 0) {
                return Result.error(400, "该号源不可预约（可能已约满或停诊）");
            }
            int locked = slotMapper.lockSlotInDb(req.getSlotId(), 1, patientId,
                    String.valueOf(System.currentTimeMillis() + 600000));
            if (locked == 0) {
                return Result.error(409, "号源已被他人锁定，请刷新重试");
            }
        }

        String orderNo = generateOrderNo();

        Appointment appointment = new Appointment();
        appointment.setOrderNo(orderNo);
        appointment.setPatientId(patientId);
        appointment.setMemberId(req.getMemberId());
        appointment.setSlotId(req.getSlotId());
        appointment.setDoctorId(req.getDoctorId());
        appointment.setDeptId(req.getDeptId());
        appointment.setFee(req.getFee());
        appointment.setStatus(AppointmentStatus.PENDING_PAY.getCode());
        appointmentMapper.insert(appointment);

        paymentService.createPaymentRecord(orderNo, req.getFee());

        OrderVO vo = new OrderVO();
        vo.setOrderNo(orderNo);
        vo.setSlotId(req.getSlotId());
        vo.setFee(req.getFee());
        vo.setExpireSeconds(600);
        vo.setQrCodeUrl("/api/payment/mock-qr?orderNo=" + orderNo);

        return Result.success("订单创建成功，请在10分钟内支付", vo);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> handlePaymentCallback(PaymentCallbackReq req) {
        Appointment appointment = appointmentMapper.selectByOrderNo(req.getOrderNo());
        if (appointment == null) {
            return Result.error(404, "订单不存在");
        }
        if (!AppointmentStatus.PENDING_PAY.getCode().equals(appointment.getStatus())) {
            return Result.success("订单已处理", null);
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED.getCode());
        appointment.setPayTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);

        slotMapper.updateStatus(appointment.getSlotId(), SlotStatus.BOOKED.getCode());

        scheduleMapper.updateBookedSlots(
                slotMapper.selectById(appointment.getSlotId()).getScheduleId(), 1);

        paymentService.confirmPayment(req.getOrderNo(), req.getTransactionId());

        notificationService.sendAppointmentSuccess(appointment);

        return Result.success("支付成功，预约已确认", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelAppointment(Long appointmentId, Long patientId, String reason) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || !appointment.getPatientId().equals(patientId)) {
            return Result.error(403, "无权操作此订单");
        }

        if (AppointmentStatus.CANCELLED.getCode().equals(appointment.getStatus())) {
            return Result.error(400, "订单已取消");
        }

        Slot slot = slotMapper.selectById(appointment.getSlotId());
        if (slot != null && slot.getStartTime().isBefore(LocalDateTime.now().plusHours(24))) {
            return Result.error(400, "就诊前24小时内不可取消");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED.getCode());
        appointment.setCancelReason(reason);
        appointment.setCancelBy(1);
        appointmentMapper.updateById(appointment);

        slotMapper.updateStatus(appointment.getSlotId(), SlotStatus.AVAILABLE.getCode());
        try {
            slotLockService.unlockSlot(appointment.getSlotId(), patientId);
        } catch (Exception e) {
            log.warn("Redis解锁失败，已通过DB释放号源: {}", e.getMessage());
        }

        if (slot != null) {
            scheduleMapper.updateBookedSlots(slot.getScheduleId(), -1);
        }

        if (AppointmentStatus.CONFIRMED.getCode().equals(appointment.getStatus())) {
            paymentService.refund(appointment.getOrderNo());
        }

        recordCancelBehavior(patientId);
        notificationService.sendCancelNotification(appointment);

        return Result.success("取消成功，费用将原路退回", null);
    }

    public Result<String> adminCancelAppointment(Long appointmentId, String reason) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            return Result.error(404, "订单不存在");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED.getCode());
        appointment.setCancelReason(reason);
        appointment.setCancelBy(2);
        appointmentMapper.updateById(appointment);

        slotMapper.updateStatus(appointment.getSlotId(), SlotStatus.AVAILABLE.getCode());

        if (AppointmentStatus.CONFIRMED.getCode().equals(appointment.getStatus())) {
            paymentService.refund(appointment.getOrderNo());
        }

        return Result.success("后台取消成功", null);
    }

    public Result<Page<AppointmentVO>> listByPatient(Long patientId, Integer status, int page, int size) {
        Page<Appointment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getPatientId, patientId);
        if (status != null) {
            wrapper.eq(Appointment::getStatus, status);
        }
        wrapper.orderByDesc(Appointment::getCreatedAt);

        Page<Appointment> result = appointmentMapper.selectPage(pageParam, wrapper);
        Page<AppointmentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return Result.success(voPage);
    }

    public Result<Page<AppointmentVO>> listAll(Integer status, Long doctorId, int page, int size) {
        Page<Appointment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Appointment::getStatus, status);
        }
        if (doctorId != null) {
            wrapper.eq(Appointment::getDoctorId, doctorId);
        }
        wrapper.orderByDesc(Appointment::getCreatedAt);

        Page<Appointment> result = appointmentMapper.selectPage(pageParam, wrapper);
        Page<AppointmentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return Result.success(voPage);
    }

    private AppointmentVO toVO(Appointment a) {
        AppointmentVO vo = new AppointmentVO();
        vo.setId(a.getId());
        vo.setOrderNo(a.getOrderNo());
        vo.setSlotId(a.getSlotId());
        vo.setDoctorId(a.getDoctorId());
        vo.setDeptId(a.getDeptId());
        vo.setFee(a.getFee());
        vo.setStatus(a.getStatus());
        vo.setPayTime(a.getPayTime());
        vo.setCancelReason(a.getCancelReason());
        vo.setCreatedAt(a.getCreatedAt());

        for (AppointmentStatus s : AppointmentStatus.values()) {
            if (s.getCode().equals(a.getStatus())) {
                vo.setStatusDesc(s.getDesc());
                break;
            }
        }

        Slot slot = slotMapper.selectById(a.getSlotId());
        if (slot != null) {
            vo.setSlotNo(slot.getSlotNo());
            vo.setVisitTime(slot.getStartTime());
        }

        Doctor doctor = doctorMapper.selectById(a.getDoctorId());
        if (doctor != null) {
            vo.setDoctorName(doctor.getName());
            vo.setDoctorTitle(doctor.getTitle());
        }

        Department dept = departmentMapper.selectById(a.getDeptId());
        if (dept != null) {
            vo.setDeptName(dept.getName());
        }

        if (a.getMemberId() != null) {
            PatientMember member = memberMapper.selectById(a.getMemberId());
            if (member != null) {
                vo.setMemberName(member.getName());
            }
        }

        return vo;
    }

    private void recordCancelBehavior(Long patientId) {
        Integer cancelCount = appointmentMapper.countRecentCancellations(
                patientId, LocalDateTime.now().minusDays(blacklistDays));

        if (cancelCount >= blacklistThreshold) {
            patientMapper.updateBlacklistEndTime(
                    patientId, LocalDateTime.now().plusDays(blacklistDays));
            log.warn("患者{}因频繁取消({})次加入黑名单", patientId, cancelCount);
        }
    }

    private String generateOrderNo() {
        return "AP" + System.currentTimeMillis() +
                String.format("%04d", new Random().nextInt(10000));
    }
}
