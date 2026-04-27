ALTER TABLE appointment
ADD COLUMN visit_status TINYINT DEFAULT 0 COMMENT '就诊状态：0未就诊，1已就诊' AFTER no_show_flag;
