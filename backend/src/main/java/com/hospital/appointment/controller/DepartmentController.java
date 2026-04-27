package com.hospital.appointment.controller;

import com.hospital.appointment.common.dto.DepartmentCreateReq;
import com.hospital.appointment.common.dto.DepartmentUpdateReq;
import com.hospital.appointment.common.result.Result;
import com.hospital.appointment.entity.Department;
import com.hospital.appointment.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/department")
@Tag(name = "科室接口")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/list")
    @Operation(summary = "科室列表")
    public Result<List<Department>> list() {
        return departmentService.list();
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','DEPT_ADMIN')")
    @Operation(summary = "新增科室")
    public Result<Department> create(@Valid @RequestBody DepartmentCreateReq req) {
        return departmentService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEPT_ADMIN')")
    @Operation(summary = "修改科室")
    public Result<Department> update(@PathVariable Long id, @Valid @RequestBody DepartmentUpdateReq req) {
        return departmentService.update(id, req);
    }
}
