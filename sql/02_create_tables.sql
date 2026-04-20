USE `hospital_db`;

-- 患者表
CREATE TABLE IF NOT EXISTS `patient` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(11) NOT NULL COMMENT '手机号',
  `password` varchar(100) NOT NULL COMMENT 'BCrypt加密密码',
  `real_name` varchar(20) DEFAULT NULL COMMENT '真实姓名',
  `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号',
  `auth_status` tinyint NOT NULL DEFAULT 0 COMMENT '认证状态:0未认证1已认证',
  `blacklist_end_time` datetime DEFAULT NULL COMMENT '黑名单截止时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_id_card` (`id_card`),
  KEY `idx_auth_status` (`auth_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者表';

-- 就诊人表（代预约）
CREATE TABLE IF NOT EXISTS `patient_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `patient_id` bigint NOT NULL COMMENT '所属患者ID',
  `name` varchar(20) NOT NULL COMMENT '姓名',
  `id_card` varchar(18) NOT NULL COMMENT '身份证号',
  `relation` varchar(10) DEFAULT NULL COMMENT '关系:父子/夫妻/其他',
  `auth_status` tinyint NOT NULL DEFAULT 0 COMMENT '认证状态',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_idcard` (`patient_id`,`id_card`),
  CONSTRAINT `fk_member_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊人表';

-- 科室表
CREATE TABLE IF NOT EXISTS `department` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '科室名称',
  `description` varchar(200) DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序权重',
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室表';

-- 医生表
CREATE TABLE IF NOT EXISTS `doctor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL COMMENT '关联系统用户ID',
  `name` varchar(20) NOT NULL COMMENT '医生姓名',
  `dept_id` bigint NOT NULL COMMENT '所属科室',
  `title` varchar(20) DEFAULT NULL COMMENT '职称:主任医师/副主任医师/主治医师/住院医师',
  `specialty` varchar(100) DEFAULT NULL COMMENT '擅长领域',
  `introduction` text COMMENT '医生简介',
  `avatar` varchar(200) DEFAULT NULL COMMENT '头像URL',
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_dept` (`dept_id`),
  CONSTRAINT `fk_doctor_dept` FOREIGN KEY (`dept_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生表';

-- 排班表
CREATE TABLE IF NOT EXISTS `schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `doctor_id` bigint NOT NULL,
  `work_date` date NOT NULL COMMENT '出诊日期',
  `shift_type` tinyint NOT NULL COMMENT '班次:1上午2下午',
  `total_slots` int NOT NULL COMMENT '总号源数',
  `booked_slots` int NOT NULL DEFAULT 0 COMMENT '已预约号源数',
  `slot_duration` int NOT NULL DEFAULT 15 COMMENT '单个号源时长(分钟)',
  `start_time` time NOT NULL COMMENT '班次开始时间',
  `end_time` time NOT NULL COMMENT '班次结束时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态:1正常0停诊',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doctor_date_shift` (`doctor_id`,`work_date`,`shift_type`),
  KEY `idx_work_date` (`work_date`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_schedule_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生排班表';

-- 号源表
CREATE TABLE IF NOT EXISTS `slot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `schedule_id` bigint NOT NULL,
  `doctor_id` bigint NOT NULL COMMENT '医生ID(冗余)',
  `dept_id` bigint NOT NULL COMMENT '科室ID(冗余)',
  `slot_no` varchar(20) NOT NULL COMMENT '号源编号',
  `start_time` datetime NOT NULL COMMENT '就诊开始时间',
  `end_time` datetime NOT NULL COMMENT '就诊结束时间',
  `fee` decimal(10,2) NOT NULL COMMENT '医事服务费',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态:0可约1锁定2已约3取消4已就诊',
  `locked_by` bigint DEFAULT NULL COMMENT '锁定时的患者ID',
  `locked_expire_time` datetime DEFAULT NULL COMMENT '锁定过期时间',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slot_no` (`slot_no`),
  KEY `idx_schedule_status` (`schedule_id`,`status`),
  KEY `idx_doctor_date` (`doctor_id`,`start_time`),
  KEY `idx_lock_expire` (`locked_expire_time`),
  CONSTRAINT `fk_slot_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='号源表';

-- 预约订单表
CREATE TABLE IF NOT EXISTS `appointment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `patient_id` bigint NOT NULL,
  `member_id` bigint DEFAULT NULL COMMENT '代预约就诊人ID',
  `slot_id` bigint NOT NULL,
  `doctor_id` bigint NOT NULL,
  `dept_id` bigint NOT NULL,
  `fee` decimal(10,2) NOT NULL,
  `status` tinyint NOT NULL COMMENT '0待支付1已确认2已取消3已完成4爽约',
  `pay_time` datetime DEFAULT NULL,
  `cancel_reason` varchar(100) DEFAULT NULL,
  `cancel_by` tinyint DEFAULT NULL COMMENT '1患者2管理员3系统',
  `no_show_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否爽约',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_patient_status` (`patient_id`,`status`),
  KEY `idx_slot` (`slot_id`),
  KEY `idx_doctor` (`doctor_id`),
  KEY `idx_created` (`created_at`),
  CONSTRAINT `fk_appoint_patient` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`),
  CONSTRAINT `fk_appoint_slot` FOREIGN KEY (`slot_id`) REFERENCES `slot` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约订单表';

-- 支付记录表（模拟）
CREATE TABLE IF NOT EXISTS `payment_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL,
  `transaction_id` varchar(64) DEFAULT NULL COMMENT '模拟交易号',
  `amount` decimal(10,2) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待支付1成功2失败3已退款',
  `pay_channel` varchar(20) NOT NULL DEFAULT 'SANDBOX',
  `callback_time` datetime DEFAULT NULL,
  `refund_time` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_transaction` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 系统用户表（管理员/医生登录）
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '登录账号',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(20) NOT NULL,
  `role_type` tinyint NOT NULL COMMENT '1超管2科室管理员3医生',
  `dept_id` bigint DEFAULT NULL COMMENT '所属科室',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1正常0禁用',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `operator_id` bigint NOT NULL,
  `operator_name` varchar(50) NOT NULL,
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
  `target_id` bigint DEFAULT NULL COMMENT '操作目标ID',
  `request_params` json DEFAULT NULL,
  `result` varchar(20) DEFAULT NULL COMMENT 'SUCCESS/FAIL',
  `error_msg` varchar(200) DEFAULT NULL,
  `ip_address` varchar(50) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_operator` (`operator_id`),
  KEY `idx_type` (`operation_type`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
