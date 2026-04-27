package com.hospital.appointment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.common.dto.AppointmentCreateReq;
import com.hospital.appointment.common.dto.PaymentCallbackReq;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.common.vo.AppointmentVO;
import com.hospital.appointment.common.vo.OrderVO;
import com.hospital.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointment")
@Tag(name = "预约接口")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 创建预约接口


     * @param authentication 认证信息，包含当前登录用户信息
     * @return 返回创建结果，包含预约信息(OrderVO)的Result对象
     *
     * 方法实现逻辑：
     * 1. 从认证信息中获取患者ID
     * 2. 调用预约服务创建预约
     * 3. 返回创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建预约")
    public Result<OrderVO> create(@Valid @RequestBody AppointmentCreateReq req, Authentication authentication) {
        // 从认证信息中获取患者ID
        Long patientId = (Long) authentication.getPrincipal();
        // 调用服务层方法创建预约并返回结果
        return appointmentService.createAppointment(req, patientId);
    }

/**
 * 获取患者预约列表的接口方法
 */
    @GetMapping("/list")  // HTTP GET请求映射到/list路径
    @Operation(summary = "我的预约列表")  // API文档中的接口描述
    public Result<Page<AppointmentVO>> list(  // 返回类型为分页的预约视图对象列表
            @RequestParam(required = false) Integer status,  // 状态参数，非必需
            @RequestParam(required = false, defaultValue = "false") Boolean history,  // 历史记录参数，默认为false
            @RequestParam(defaultValue = "1") int page,  // 页码参数，默认为1
            @RequestParam(defaultValue = "10") int size,  // 页大小参数，默认为10
            Authentication authentication) {  // 认证信息参数
        Long patientId = (Long) authentication.getPrincipal();  // 从认证信息中获取患者ID
        return appointmentService.listByPatient(patientId, status, history, page, size);  // 调用服务层方法获取预约列表
    }

/**
 * 取消预约的接口方法
*/
    @PostMapping("/cancel/{id}")    // 设置POST请求映射，指定请求路径为/cancel/{id}
    @Operation(summary = "取消预约")    // API文档说明，标注此接口用于取消预约
    public Result<String> cancel(    // 定义返回类型为Result<String>的接口方法
            @PathVariable Long id,    // 从路径中获取预约ID
            @RequestParam(required = false, defaultValue = "") String reason,    // 获取取消原因参数，非必需，默认为空
            Authentication authentication) {    // 获取当前认证信息
        Long patientId = (Long) authentication.getPrincipal();    // 从认证信息中获取患者ID
        return appointmentService.cancelAppointment(id, patientId, reason);    // 调用服务层方法取消预约并返回结果
    }
}
