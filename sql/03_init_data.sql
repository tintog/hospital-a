USE `hospital_db`;

-- 初始管理员 (密码: 123456, BCrypt加密)
INSERT INTO sys_user (username, password, real_name, role_type, dept_id) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1, NULL),
('dept_admin_nk', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '内科管理员', 2, 1),
('dept_admin_wk', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '外科管理员', 2, 2),
('doctor_zhang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张医生', 3, 1),
('doctor_li', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李医生', 3, 1),
('doctor_wang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王医生', 3, 2);

-- 科室数据
INSERT INTO department (name, description, sort_order) VALUES
('内科', '呼吸内科/消化内科/心血管内科/神经内科/内分泌科', 1),
('外科', '普外科/骨科/泌尿外科/神经外科/胸外科', 2),
('儿科', '儿童常见病/新生儿科/小儿外科', 3),
('妇产科', '妇科/产科/计划生育', 4),
('眼科', '白内障/青光眼/眼底病/屈光手术', 5),
('口腔科', '牙体牙髓/牙周/口腔修复/口腔颌面外科', 6),
('皮肤科', '皮肤病/性病/医学美容', 7),
('中医科', '中医内科/针灸推拿/中医妇科', 8);

-- 医生数据
INSERT INTO doctor (user_id, name, dept_id, title, specialty, introduction) VALUES
(4, '张伟', 1, '主任医师', '心血管疾病', '从医30年，擅长高血压、冠心病、心律失常等心血管疾病的诊治，主持多项省级科研课题。'),
(5, '李芳', 1, '副主任医师', '呼吸系统疾病', '擅长哮喘、慢性阻塞性肺疾病、肺部感染等呼吸系统疾病的诊断和治疗。'),
(6, '王强', 2, '主治医师', '普通外科', '擅长阑尾炎、疝气、胆囊结石等常见外科手术，微创手术经验丰富。'),
(NULL, '赵敏', 1, '主任医师', '消化系统疾病', '擅长胃炎、肠炎、肝病等消化系统疾病，胃肠镜诊疗经验丰富。'),
(NULL, '刘洋', 2, '副主任医师', '骨科', '擅长骨折、关节置换、脊柱疾病等骨科疾病的手术治疗。'),
(NULL, '陈静', 3, '主任医师', '儿童呼吸', '擅长儿童哮喘、反复呼吸道感染、小儿肺炎等儿科疾病。'),
(NULL, '孙丽', 4, '副主任医师', '妇科肿瘤', '擅长子宫肌瘤、卵巢囊肿等妇科良恶性肿瘤的微创治疗。'),
(NULL, '周明', 5, '主治医师', '白内障', '擅长白内障超声乳化手术、人工晶体植入术。');

-- 测试患者 (密码: 123456)
INSERT INTO patient (phone, password, real_name, id_card, auth_status) VALUES
('13800138000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', '110101199001011234', 1),
('13800138001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', '110101199202022345', 1),
('13800138002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五', '110101198805053456', 1);

-- 就诊人数据
INSERT INTO patient_member (patient_id, name, id_card, relation, auth_status) VALUES
(1, '张小明', '110101201501014567', '父子', 1),
(1, '李梅', '110101199203035678', '夫妻', 1),
(2, '李小红', '110101201803036789', '父女', 1);
