package com.hospital.appointment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.hospital.appointment.mapper")
@EnableScheduling
public class AppointmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppointmentApplication.class, args);
    }
}
