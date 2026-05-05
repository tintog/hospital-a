package com.hospital.appointment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.entity.Slot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface SlotMapper extends BaseMapper<Slot> {

    @Update("UPDATE slot SET status = #{status}, locked_by = NULL, locked_expire_time = NULL WHERE id = #{slotId}")
    int updateStatus(@Param("slotId") Long slotId, @Param("status") Integer status);

    @Update("UPDATE slot SET status = #{status}, locked_by = #{lockedBy}, locked_expire_time = #{expireTime} WHERE id = #{slotId} AND status = 0")
    int lockSlotInDb(@Param("slotId") Long slotId, @Param("status") Integer status,
                     @Param("lockedBy") Long lockedBy, @Param("expireTime") LocalDateTime expireTime);
}
