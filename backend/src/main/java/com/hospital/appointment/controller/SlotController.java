package com.hospital.appointment.controller;

import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.common.vo.SlotVO;
import com.hospital.appointment.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slot")
@Tag(name = "号源接口")
public class SlotController {

    @Autowired
    private SlotService slotService;

    @GetMapping("/calendar")
    @Operation(summary = "号源日历")
    public Result<List<SlotVO>> calendar(
            @RequestParam Long doctorId,
            @RequestParam String date) {
        return slotService.getSlotsByDoctorAndDate(doctorId, date);
    }
}
