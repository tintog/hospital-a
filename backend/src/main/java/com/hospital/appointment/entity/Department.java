package com.hospital.appointment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import lombok.Data; // 使用Lombok的@Data注解自动生成getter、setter等方法
import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus表名注解
import com.baomidou.mybatisplus.annotation.IdType; // MyBatis-Plus ID类型枚举
import com.baomidou.mybatisplus.annotation.TableId; // MyBatis-Plus主键注解
import com.baomidou.mybatisplus.annotation.TableLogic; // MyBatis-Plus逻辑删除注解

/**
 * 部门实体类
 * 对应数据库中的department表
 */
@Data // 自动生成getter、setter、toString、equals、hashCode等方法
@TableName("department") // 指定此实体类对应的数据库表名为department
public class Department {


    /**部门ID
     使用自增策略作为主键*/
    @TableId(type = IdType.AUTO) // 指定此字段为主键，并使用自增策略
    private Long id;
    /** 部门名称*/
    private String name;
    /** 部门描述*/
    private String description;
    /**排序序号
     * 用于部门排序显示*/
    private Integer sortOrder;


    /** 逻辑删除标志
     * 1表示已删除，0表示未删除
     * 使用@TableLogic注解实现逻辑删除功能*/
    @TableLogic // 标记此字段为逻辑删除字段
    private Integer deleted;
}
