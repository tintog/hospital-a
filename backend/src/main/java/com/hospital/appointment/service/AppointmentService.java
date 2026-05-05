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

/**
 * 预约服务类，处理预约相关的业务逻辑
 */
@Service
@Slf4j
public class AppointmentService {

    @Autowired
    private SlotLockService slotLockService; // 号源锁服务，用于处理号源的锁定和解锁
    @Autowired
    private AppointmentMapper appointmentMapper; // 预约数据访问层
    @Autowired
    private SlotMapper slotMapper; // 号源数据访问层
    @Autowired
    private PatientMapper patientMapper; // 患者数据访问层
    @Autowired
    private DoctorMapper doctorMapper; // 医生数据访问层
    @Autowired
    private DepartmentMapper departmentMapper; // 科室数据访问层
    @Autowired
    private PatientMemberMapper memberMapper; // 患者会员数据访问层
    @Autowired
    private ScheduleMapper scheduleMapper; // 排班数据访问层
    @Autowired
    private PaymentService paymentService; // 支付服务
    @Autowired
    private NotificationService notificationService; // 通知服务

    // 预约黑名单阈值配置
    @Value("${appointment.blacklist-threshold:3}")
    private Integer blacklistThreshold;

    // 预约黑名单天数配置
    @Value("${appointment.blacklist-days:30}")
    private Integer blacklistDays;

    /**
     * 创建预约订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<OrderVO> createAppointment(AppointmentCreateReq req, Long patientId) {
        // 检查患者是否存在
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            return Result.error(400, "患者不存在");
        }
        // 检查患者是否在黑名单中
        if (patient.getBlacklistEndTime() != null &&
                patient.getBlacklistEndTime().isAfter(LocalDateTime.now())) {
            return Result.error(403, "您已被限制预约，解除时间: " + patient.getBlacklistEndTime());
        }

        // 检查号源是否存在
        Slot slot = slotMapper.selectById(req.getSlotId());
        if (slot == null) {
            return Result.error(400, "号源不存在");
        }

        boolean lockedByRedis = false;
        boolean lockedByDb = false;

        // Try Redis lock first, fallback to DB lock if Redis is unavailable
        try {
            Long lockResult = slotLockService.lockSlot(req.getSlotId(), patientId);
            if (lockResult == -1L) {
                return Result.error(400, "该号源不可预约（可能已约满或停诊）");
            } else if (lockResult == 0L) {
                return Result.error(409, "号源已被他人锁定，请刷新重试");
            }
            lockedByRedis = true;
        } catch (Exception e) {
            log.warn("Redis锁不可用，降级为数据库锁: {}", e.getMessage());
            if (slot.getStatus() != 0) {
                return Result.error(400, "该号源不可预约（可能已约满或停诊）");
            }
            int locked = slotMapper.lockSlotInDb(req.getSlotId(), 1, patientId,
                    LocalDateTime.now().plusMinutes(10));
            if (locked == 0) {
                return Result.error(409, "号源已被他人锁定，请刷新重试");
            }
            lockedByDb = true;
        }

/**
 * 创建预约订单
 * 1. 生成订单号并创建预约记录
 * 2. 创建支付记录
 * 3. 返回订单信息，包含支付二维码
 */
        String orderNo = generateOrderNo(); // 生成唯一订单号

        Appointment appointment = new Appointment(); // 创建预约对象
        appointment.setOrderNo(orderNo); // 设置订单号
        appointment.setPatientId(patientId); // 设置患者ID
        appointment.setMemberId(req.getMemberId()); // 设置会员ID
        appointment.setSlotId(req.getSlotId()); // 设置时间段ID
        appointment.setDoctorId(req.getDoctorId()); // 设置医生ID
        appointment.setDeptId(req.getDeptId()); // 设置科室ID
        appointment.setFee(req.getFee()); // 设置费用
        appointment.setStatus(AppointmentStatus.PENDING_PAY.getCode()); // 设置订单状态为待支付
        appointment.setVisitStatus(0); // 设置就诊状态为未就诊
        appointmentMapper.insert(appointment); // 插入预约记录到数据库

        try {
            paymentService.createPaymentRecord(orderNo, req.getFee()); // 创建支付记录：根据订单号和费用创建支付记录
        } catch (Exception e) {
            if (lockedByRedis) { // 判断是否使用了Redis分布式锁
                try {
                    // 尝试释放Redis分布式锁，如果创建支付记录失败，需要释放锁
                    slotLockService.unlockSlot(req.getSlotId(), patientId);
                } catch (Exception unlockEx) {
                    // 记录释放Redis锁失败的警告日志，但不影响主流程
                    log.warn("支付记录创建失败后Redis解锁失败: {}", unlockEx.getMessage());
                }
            }
            if (lockedByDb) { // 判断是否使用了数据库锁
                // 如果使用了数据库锁，且支付记录创建失败，则将时间段状态更新为可用
                slotMapper.updateStatus(req.getSlotId(), SlotStatus.AVAILABLE.getCode());
            }
            throw e;
        }

        OrderVO vo = new OrderVO(); // 创建返回视图对象
        vo.setOrderNo(orderNo);
        vo.setSlotId(req.getSlotId());
        vo.setFee(req.getFee());
        vo.setExpireSeconds(600); // 设置支付过期时间（10分钟）
        vo.setQrCodeUrl("/api/payment/mock-qr?orderNo=" + orderNo); // 设置支付二维码URL

        return Result.success("订单创建成功，请在10分钟内支付", vo); // 返回成功响应
    }

    /**
     * 处理支付回调
     * 1. 验证订单是否存在及状态
     * 2. 更新订单状态为已确认
     * 3. 更新号源状态为已预约
     * 4. 更新排班表已预约数量
     * 5. 确认支付并发送成功通知
     */
    @Transactional(rollbackFor = Exception.class) // 使用事务注解，确保方法内所有操作要么全部成功，要么全部回滚
    public Result<String> handlePaymentCallback(PaymentCallbackReq req) { // 支付回调处理方法，接收支付回调请求参数
        Appointment appointment = appointmentMapper.selectByOrderNo(req.getOrderNo()); // 根据订单号查询预约信息
        if (appointment == null) { // 检查订单是否存在
            return Result.error(404, "订单不存在"); // 如果订单不存在，返回错误信息
        }
        if (!AppointmentStatus.PENDING_PAY.getCode().equals(appointment.getStatus())) { // 检查订单状态
            return Result.success("订单已处理", null); // 如果订单状态不是待支付，表示已处理过，直接返回成功
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED.getCode()); // 更新订单状态为已确认
        appointment.setPayTime(LocalDateTime.now()); // 设置支付时间为当前时间
        appointmentMapper.updateById(appointment); // 更新订单信息到数据库

        slotMapper.updateStatus(appointment.getSlotId(), SlotStatus.BOOKED.getCode()); // 更新号源状态为已预约

        scheduleMapper.updateBookedSlots( // 更新排班表已预约数量
                slotMapper.selectById(appointment.getSlotId()).getScheduleId(), 1); // 获取号源所属的排班ID，已预约数量加1

        paymentService.confirmPayment(req.getOrderNo(), req.getTransactionId()); // 确认支付，使用订单号和交易ID

        notificationService.sendAppointmentSuccess(appointment); // 发送预约成功通知，包含预约详情

        return Result.success("支付成功，预约已确认", null); // 返回成功结果，表示支付处理完成
    }

    /**
     * 取消预约
     * 1. 验证订单权限和状态
     * 2. 检查是否在可取消时间范围内
     * 3. 更新订单状态为已取消
     * 4. 释放号源并更新排班表
     * 5. 处理退款
     * 6. 记录取消行为并发送通知
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelAppointment(Long appointmentId, Long patientId, String reason) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || !appointment.getPatientId().equals(patientId)) { // 验证订单权限
            return Result.error(403, "无权操作此订单");
        }

        if (AppointmentStatus.CANCELLED.getCode().equals(appointment.getStatus())) { // 检查订单状态
            return Result.error(400, "订单已取消");
        }

       Slot slot = slotMapper.selectById(appointment.getSlotId());
        //if (slot != null && slot.getStartTime().isBefore(LocalDateTime.now().plusHours(24))) { // 检查是否在可取消时间范围内
       //     return Result.error(400, "就诊前24小时内不可取消");
       // }

        Integer originalStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.CANCELLED.getCode()); // 更新订单状态为已取消
        appointment.setCancelReason(reason); // 设置取消原因
        appointment.setCancelBy(1); // 设置取消方（1-患者）
        appointmentMapper.updateById(appointment); // 更新订单信息

        slotMapper.updateStatus(appointment.getSlotId(), SlotStatus.AVAILABLE.getCode()); // 释放号源
        try {
            slotLockService.unlockSlot(appointment.getSlotId(), patientId); // 解锁号源
        } catch (Exception e) {
            log.warn("Redis解锁失败，已通过DB释放号源: {}", e.getMessage());
        }

        if (slot != null && AppointmentStatus.CONFIRMED.getCode().equals(originalStatus)) { // 仅已确认订单回滚已约数
            scheduleMapper.decreaseBookedSlotsSafely(slot.getScheduleId());
        }

        if (AppointmentStatus.CONFIRMED.getCode().equals(originalStatus)) { // 处理退款
            paymentService.refund(appointment.getOrderNo());
        }

        recordCancelBehavior(patientId); // 记录取消行为
        notificationService.sendCancelNotification(appointment); // 发送取消通知

        return Result.success("取消成功，费用将原路退回", null);
    }

    /**
     * 后台取消预约
     * 1. 验证订单是否存在
     * 2. 更新订单状态为已取消
     * 3. 释放号源
     * 4. 处理退款
     */
    public Result<String> adminCancelAppointment(Long appointmentId, String reason) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) { // 检查订单是否存在
            return Result.error(404, "订单不存在");
        }

        Integer originalStatus = appointment.getStatus();

        appointment.setStatus(AppointmentStatus.CANCELLED.getCode()); // 更新订单状态为已取消
        appointment.setCancelReason(reason); // 设置取消原因
        appointment.setCancelBy(2); // 设置取消方（2-管理员）
        appointmentMapper.updateById(appointment); // 更新订单信息

        slotMapper.updateStatus(appointment.getSlotId(), SlotStatus.AVAILABLE.getCode()); // 释放号源

        Slot slot = slotMapper.selectById(appointment.getSlotId());
        if (slot != null && AppointmentStatus.CONFIRMED.getCode().equals(originalStatus)) {
            scheduleMapper.decreaseBookedSlotsSafely(slot.getScheduleId());
        }

        if (AppointmentStatus.CONFIRMED.getCode().equals(originalStatus)) { // 处理退款
            paymentService.refund(appointment.getOrderNo());
        }

        return Result.success("后台取消成功", null);
    }

    /**
     * 查询患者预约列表
     * @param patientId 患者ID
     * @param status 预约状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 预约列表
     */
    public Result<Page<AppointmentVO>> listByPatient(Long patientId, Integer status, Boolean history, int page, int size) {
        // 创建分页参数对象，指定当前页码和每页大小
        Page<Appointment> pageParam = new Page<>(page, size);
        // 创建Lambda查询包装器，并设置患者ID作为查询条件
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getPatientId, patientId); // 设置患者ID条件
        // 如果状态参数不为空，则添加状态条件到查询包装器中
        if (status != null) { // 设置状态条件（可选）
            wrapper.eq(Appointment::getStatus, status);
        }

        // 根据history参数决定查询历史预约还是当前预约
        if (Boolean.TRUE.equals(history)) {
            // 查询历史预约：包括已取消、已就诊和已完成状态的预约
            wrapper.and(w -> w.eq(Appointment::getStatus, AppointmentStatus.CANCELLED.getCode())
                    .or().eq(Appointment::getVisitStatus, 1)
                    .or().eq(Appointment::getStatus, AppointmentStatus.COMPLETED.getCode()));
        } else {
            // 查询当前预约：排除已取消和已完成的预约，且未就诊或就诊状态为0
            wrapper.ne(Appointment::getStatus, AppointmentStatus.CANCELLED.getCode())
                    .ne(Appointment::getStatus, AppointmentStatus.COMPLETED.getCode())
                    .and(w -> w.isNull(Appointment::getVisitStatus).or().eq(Appointment::getVisitStatus, 0));
        }

        // 按创建时间降序排序结果
        wrapper.orderByDesc(Appointment::getCreatedAt); // 按创建时间降序排序

        // 执行分页查询，获取预约记录
        Page<Appointment> result = appointmentMapper.selectPage(pageParam, wrapper);
        // 创建VO分页对象，复制分页信息
        Page<AppointmentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        // 将预约记录列表转换为VO对象列表
        voPage.setRecords(result.getRecords().stream().map(this::toVO).collect(Collectors.toList())); // 转换为VO对象
        // 返回成功结果，包含转换后的VO分页数据
        return Result.success(voPage);
    }

/**
 * 根据医生ID查询预约列表
 * @return 返回分页后的预约结果，包含预约信息视图对象(AppointmentVO)
 */
    public Result<Page<AppointmentVO>> listByDoctor(Long doctorId, Boolean history, int page, int size) {
    // 创建分页参数对象
        Page<Appointment> pageParam = new Page<>(page, size);
    // 创建查询条件构造器，设置医生ID筛选条件
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getDoctorId, doctorId);

    // 根据history参数决定查询历史记录还是当前预约
        if (Boolean.TRUE.equals(history)) {
        // 查询历史记录条件：已取消、已到访或已完成状态的预约
            wrapper.and(w -> w.eq(Appointment::getStatus, AppointmentStatus.CANCELLED.getCode())
                    .or().eq(Appointment::getVisitStatus, 1)
                    .or().eq(Appointment::getStatus, AppointmentStatus.COMPLETED.getCode()));
        } else {
        // 查询当前预约条件：已确认状态且未到访的预约
            wrapper.eq(Appointment::getStatus, AppointmentStatus.CONFIRMED.getCode())
                    .and(w -> w.isNull(Appointment::getVisitStatus).or().eq(Appointment::getVisitStatus, 0));
        }

    // 按创建时间升序排序
        wrapper.orderByAsc(Appointment::getCreatedAt);
    // 执行分页查询
        Page<Appointment> result = appointmentMapper.selectPage(pageParam, wrapper);

    // 创建结果VO分页对象，并转换记录为VO对象
        Page<AppointmentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
    // 返回成功结果
        return Result.success(voPage);
    }

    /**
     * 查询所有预约列表（管理员）
     * @param status 预约状态（可选）
     * @param doctorId 医生ID（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 预约列表
     */
    public Result<Page<AppointmentVO>> listAll(Integer status, Long doctorId, String doctorName, int page, int size) {
        Page<Appointment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        if (status != null) { // 设置状态条件（可选）
            wrapper.eq(Appointment::getStatus, status);
        }
        if (doctorId != null) { // 设置医生ID条件（可选）
            wrapper.eq(Appointment::getDoctorId, doctorId);
        }
        if (doctorName != null && !doctorName.trim().isEmpty()) {
            List<Doctor> doctors = doctorMapper.selectList(new LambdaQueryWrapper<Doctor>()
                    .like(Doctor::getName, doctorName.trim())
                    .select(Doctor::getId));
            if (doctors == null || doctors.isEmpty()) {
                return Result.success(new Page<>(page, size, 0));
            }
            List<Long> doctorIds = doctors.stream().map(Doctor::getId).toList();
            wrapper.in(Appointment::getDoctorId, doctorIds);
        }
        wrapper.orderByDesc(Appointment::getCreatedAt); // 按创建时间降序排序

        Page<Appointment> result = appointmentMapper.selectPage(pageParam, wrapper);
        Page<AppointmentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).collect(Collectors.toList())); // 转换为VO对象
        return Result.success(voPage);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateVisitStatus(Long doctorId, Long appointmentId, Integer visitStatus) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            return Result.error(404, "预约不存在");
        }
        if (!appointment.getDoctorId().equals(doctorId)) {
            return Result.error(403, "无权操作该预约");
        }
        if (!AppointmentStatus.CONFIRMED.getCode().equals(appointment.getStatus())
                && !AppointmentStatus.COMPLETED.getCode().equals(appointment.getStatus())) {
            return Result.error(400, "当前预约状态不允许设置就诊状态");
        }
        if (visitStatus == null || (visitStatus != 0 && visitStatus != 1)) {
            return Result.error(400, "就诊状态参数错误");
        }

        appointment.setVisitStatus(visitStatus);
        if (visitStatus == 1 && AppointmentStatus.CONFIRMED.getCode().equals(appointment.getStatus())) {
            appointment.setStatus(AppointmentStatus.COMPLETED.getCode());
        }
        appointmentMapper.updateById(appointment);
        return Result.success("更新成功", null);
    }

    /**
     * 将预约对象转换为视图对象
     * @param a 预约对象
     * @return 预约视图对象
     */
    private AppointmentVO toVO(Appointment a) {
    // 创建新的预约视图对象
        AppointmentVO vo = new AppointmentVO();
    // 设置预约基本信息
        vo.setId(a.getId()); // 设置预约ID
        vo.setOrderNo(a.getOrderNo()); // 设置订单号
        vo.setSlotId(a.getSlotId()); // 设置时间段ID
        vo.setDoctorId(a.getDoctorId()); // 设置医生ID
        vo.setDeptId(a.getDeptId()); // 设置科室ID
        vo.setFee(a.getFee()); // 设置费用
        vo.setStatus(a.getStatus()); // 设置状态
        vo.setPayTime(a.getPayTime()); // 设置支付时间
        vo.setCancelReason(a.getCancelReason()); // 设置取消原因
        vo.setCreatedAt(a.getCreatedAt()); // 设置创建时间
        vo.setVisitStatus(a.getVisitStatus() == null ? 0 : a.getVisitStatus());
        vo.setVisitStatusDesc((a.getVisitStatus() != null && a.getVisitStatus() == 1) ? "已就诊" : "未就诊");

        // 设置状态描述
    // 遍历所有预约状态，找到匹配的状态码并设置对应的状态描述
        for (AppointmentStatus s : AppointmentStatus.values()) {
            if (s.getCode().equals(a.getStatus())) {
                vo.setStatusDesc(s.getDesc());
                break;
            }
        }

        // 设置时间段信息
    // 根据时间段ID查询时间段对象，并设置相关信息
        Slot slot = slotMapper.selectById(a.getSlotId());
        if (slot != null) {
            vo.setSlotNo(slot.getSlotNo()); // 设置时间段编号
            vo.setVisitTime(slot.getStartTime()); // 设置就诊时间
        }

        // 设置医生信息
    // 根据医生ID查询医生对象，并设置相关信息
        Doctor doctor = doctorMapper.selectById(a.getDoctorId());
        if (doctor != null) {
            vo.setDoctorName(doctor.getName()); // 设置医生姓名
            vo.setDoctorTitle(doctor.getTitle()); // 设置医生职称
        }

        // 设置科室信息
    // 根据科室ID查询科室对象，并设置科室名称
        Department dept = departmentMapper.selectById(a.getDeptId());
        if (dept != null) {
            vo.setDeptName(dept.getName()); // 设置科室名称
        }

        // 设置患者信息
        Patient patient = patientMapper.selectById(a.getPatientId());
        if (patient != null) {
            vo.setPatientName(patient.getRealName());
        }

        // 设置会员信息
    // 如果有会员ID，则查询会员对象并设置会员姓名
        if (a.getMemberId() != null) {
            PatientMember member = memberMapper.selectById(a.getMemberId());
            if (member != null) {
                vo.setMemberName(member.getName()); // 设置会员姓名
            }
        }

    // 返回填充好的视图对象
        return vo;
    }

    /**
     * 记录患者取消行为
     * 1. 统计短期内取消次数
     * 2. 如果超过阈值，将患者加入黑名单
     * @param patientId 患者ID
     */
    private void recordCancelBehavior(Long patientId) {
        // 统计在黑名单天数内的取消次数
        Integer cancelCount = appointmentMapper.countRecentCancellations(
                patientId, LocalDateTime.now().minusDays(blacklistDays));

        // 如果取消次数超过阈值，加入黑名单
        if (cancelCount >= blacklistThreshold) {
            patientMapper.updateBlacklistEndTime(
                    patientId, LocalDateTime.now().plusDays(blacklistDays));
            log.warn("患者{}因频繁取消({})次加入黑名单", patientId, cancelCount);
        }
    }

    /**
     * 生成订单号
     * 格式：AP + 时间戳 + 4位随机数
     * @return 订单号
     */
    private String generateOrderNo() {
        return "AP" + System.currentTimeMillis() + // 添加时间戳
                String.format("%04d", new Random().nextInt(10000)); // 添加4位随机数
    }
}
