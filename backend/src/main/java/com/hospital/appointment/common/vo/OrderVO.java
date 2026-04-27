package com.hospital.appointment.common.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 订单值对象(VO)，用于封装订单相关的数据
 * 使用@Data注解自动生成getter、setter、toString等方法
 */
@Data
public class OrderVO {
    private String orderNo;    // 订单编号，唯一标识一个订单
    private Long slotId;       // 时间槽ID，标识订单所属的时间段
    private BigDecimal fee;    // 订单金额，使用BigDecimal确保金额计算的精确性
    private Integer expireSeconds; // 订单过期时间，单位为秒
    private String qrCodeUrl;  // 二维码URL，用于支付或订单展示
}
