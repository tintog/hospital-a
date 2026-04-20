USE `hospital_db`;

-- 生成未来7天的排班数据
-- 医生1(张伟-内科) 每天上午
INSERT INTO schedule (doctor_id, work_date, shift_type, total_slots, booked_slots, slot_duration, start_time, end_time, status) VALUES
(1, CURDATE(), 1, 10, 0, 15, '08:00:00', '10:30:00', 1),
(1, CURDATE(), 2, 8, 0, 15, '14:00:00', '16:00:00', 1),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, 10, 0, 15, '08:00:00', '10:30:00', 1),
(1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 1, 10, 0, 15, '08:00:00', '10:30:00', 1),
(1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 2, 8, 0, 15, '14:00:00', '16:00:00', 1),
(1, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 1, 10, 0, 15, '08:00:00', '10:30:00', 1),
(1, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 1, 10, 0, 15, '08:00:00', '10:30:00', 1);

-- 医生2(李芳-内科)
INSERT INTO schedule (doctor_id, work_date, shift_type, total_slots, booked_slots, slot_duration, start_time, end_time, status) VALUES
(2, CURDATE(), 2, 8, 0, 15, '14:00:00', '16:00:00', 1),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, 10, 0, 15, '08:00:00', '10:30:00', 1),
(2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 8, 0, 15, '14:00:00', '16:00:00', 1),
(2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 1, 10, 0, 15, '08:00:00', '10:30:00', 1),
(2, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 2, 8, 0, 15, '14:00:00', '16:00:00', 1);

-- 医生3(王强-外科)
INSERT INTO schedule (doctor_id, work_date, shift_type, total_slots, booked_slots, slot_duration, start_time, end_time, status) VALUES
(3, CURDATE(), 1, 8, 0, 20, '08:00:00', '10:40:00', 1),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 2, 6, 0, 20, '14:00:00', '16:00:00', 1),
(3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 1, 8, 0, 20, '08:00:00', '10:40:00', 1),
(3, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 1, 8, 0, 20, '08:00:00', '10:40:00', 1);

-- 医生4(赵敏-内科)
INSERT INTO schedule (doctor_id, work_date, shift_type, total_slots, booked_slots, slot_duration, start_time, end_time, status) VALUES
(4, CURDATE(), 1, 10, 0, 15, '08:00:00', '10:30:00', 1),
(4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 2, 8, 0, 15, '14:00:00', '16:00:00', 1),
(4, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 1, 10, 0, 15, '08:00:00', '10:30:00', 1);

-- 医生5(刘洋-外科)
INSERT INTO schedule (doctor_id, work_date, shift_type, total_slots, booked_slots, slot_duration, start_time, end_time, status) VALUES
(5, CURDATE(), 2, 6, 0, 20, '14:00:00', '16:00:00', 1),
(5, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, 8, 0, 20, '08:00:00', '10:40:00', 1),
(5, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 6, 0, 20, '14:00:00', '16:00:00', 1);

-- 医生6(陈静-儿科)
INSERT INTO schedule (doctor_id, work_date, shift_type, total_slots, booked_slots, slot_duration, start_time, end_time, status) VALUES
(6, CURDATE(), 1, 12, 0, 10, '08:00:00', '10:00:00', 1),
(6, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, 12, 0, 10, '08:00:00', '10:00:00', 1),
(6, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 10, 0, 10, '14:00:00', '15:40:00', 1);

-- 为排班生成号源的存储过程
DROP PROCEDURE IF EXISTS generate_slots;
DELIMITER //
CREATE PROCEDURE generate_slots()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_schedule_id BIGINT;
    DECLARE v_doctor_id BIGINT;
    DECLARE v_dept_id BIGINT;
    DECLARE v_work_date DATE;
    DECLARE v_total_slots INT;
    DECLARE v_slot_duration INT;
    DECLARE v_start_time TIME;
    DECLARE v_fee DECIMAL(10,2);
    DECLARE v_title VARCHAR(20);
    DECLARE i INT;
    DECLARE v_slot_start DATETIME;
    DECLARE v_slot_end DATETIME;
    
    DECLARE cur CURSOR FOR 
        SELECT s.id, s.doctor_id, d.dept_id, s.work_date, s.total_slots, s.slot_duration, s.start_time, d.title
        FROM schedule s JOIN doctor d ON s.doctor_id = d.id
        WHERE s.deleted = 0;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_schedule_id, v_doctor_id, v_dept_id, v_work_date, v_total_slots, v_slot_duration, v_start_time, v_title;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 根据职称设置费用
        SET v_fee = CASE v_title
            WHEN '主任医师' THEN 80.00
            WHEN '副主任医师' THEN 60.00
            WHEN '主治医师' THEN 30.00
            ELSE 15.00
        END;
        
        SET i = 0;
        WHILE i < v_total_slots DO
            SET v_slot_start = TIMESTAMP(v_work_date, ADDTIME(v_start_time, SEC_TO_TIME(i * v_slot_duration * 60)));
            SET v_slot_end = TIMESTAMP(v_work_date, ADDTIME(v_start_time, SEC_TO_TIME((i + 1) * v_slot_duration * 60)));
            
            INSERT IGNORE INTO slot (schedule_id, doctor_id, dept_id, slot_no, start_time, end_time, fee, status)
            VALUES (
                v_schedule_id,
                v_doctor_id,
                v_dept_id,
                CONCAT('S', v_schedule_id, '-', LPAD(i + 1, 3, '0')),
                v_slot_start,
                v_slot_end,
                v_fee,
                0
            );
            
            SET i = i + 1;
        END WHILE;
    END LOOP;
    CLOSE cur;
END //
DELIMITER ;

-- 执行存储过程生成号源
CALL generate_slots();

-- 清理存储过程
DROP PROCEDURE IF EXISTS generate_slots;
