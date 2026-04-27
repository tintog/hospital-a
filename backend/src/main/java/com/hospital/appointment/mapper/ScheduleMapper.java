package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

/**
 * 更新预定时间的接口方法
 * 使用@Update注解定义SQL更新语句
 *
 * @param scheduleId 排程ID，用于指定要更新的排程记录
 * @param delta 预定时间的增量值，表示要增加的预定时段数量
 * @return 返回受影响的行数，表示更新是否成功
 */
    @Update("UPDATE schedule SET booked_slots = booked_slots + #{delta} WHERE id = #{scheduleId}")
    int updateBookedSlots(@Param("scheduleId") Long scheduleId, @Param("delta") int delta);
}
