package com.hospital.appointment.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleType {
    ADMIN(1, "ADMIN", "超级管理员"),
    DEPT_ADMIN(2, "DEPT_ADMIN", "科室管理员"),
    DOCTOR(3, "DOCTOR", "医生");

    private final Integer code;
    private final String role;
    private final String desc;

    public static String getRoleByCode(Integer code) {
        for (RoleType r : values()) {
            if (r.getCode().equals(code)) {
                return r.getRole();
            }
        }
        return "UNKNOWN";
    }
}
