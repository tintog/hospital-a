package com.hospital.appointment.controller;

import com.hospital.appointment.common.dto.PaymentCallbackReq;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@Tag(name = "支付接口")
public class PaymentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/mock-callback")
    @Operation(summary = "模拟支付回调")
    public Result<String> mockCallback(@Valid @RequestBody PaymentCallbackReq req) {
        return appointmentService.handlePaymentCallback(req);
    }

    @GetMapping("/mock-qr")
    @Operation(summary = "模拟支付二维码")
    public Result<String> mockQr(@RequestParam String orderNo) {
        return Result.success("模拟支付二维码", "data:image/svg+xml," +
                "<svg xmlns='http://www.w3.org/2000/svg' width='200' height='200'>" +
                "<rect width='200' height='200' fill='white'/>" +
                "<text x='100' y='90' text-anchor='middle' font-size='14'>模拟支付</text>" +
                "<text x='100' y='120' text-anchor='middle' font-size='10'>" + orderNo + "</text>" +
                "</svg>");
    }
}
