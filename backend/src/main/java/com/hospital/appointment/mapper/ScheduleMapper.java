package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    @Update("UPDATE schedule SET booked_slots = booked_slots + #{delta} WHERE id = #{scheduleId}")
    int updateBookedSlots(@Param("scheduleId") Long scheduleId, @Param("delta") int delta);
}
