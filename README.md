# 医院预约挂号系统

> 基于 Spring Boot 3.x + Vue3 + Element Plus + MySQL + Redis 的医院在线预约挂号平台

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2.4、MyBatis-Plus 3.5.7、Spring Security、JWT、Redis |
| 前端 | Vue 3、Element Plus、Vite 5、Pinia、Vue Router 4、ECharts |
| 数据库 | MySQL 8.x、Redis 3.0+ |
| 部署 | Docker、Docker Compose、Nginx |

---

## 项目结构

```
ting300/
├── backend/                        # Spring Boot 后端
│   ├── src/main/java/com/hospital/appointment/
│   │   ├── common/                 # 通用模块(JWT/Security/Result/Exception)
│   │   ├── controller/             # 控制器(8个)
│   │   ├── service/                # 业务服务(11个)
│   │   ├── mapper/                 # MyBatis-Plus Mapper(10个)
│   │   └── entity/                 # 实体类(10个)
│   └── src/main/resources/         # 配置文件
├── frontend/                       # Vue3 前端
│   └── src/
│       ├── api/                    # API 模块(6个)
│       ├── store/                  # Pinia 状态管理
│       ├── router/                 # 路由 + 权限守卫
│       ├── layouts/                # 布局组件(患者/管理/医生)
│       └── views/                  # 页面(患者8个 + 管理6个 + 医生2个)
├── sql/                            # SQL 初始化脚本(4个)
└── docker-compose.yml
```

---

## 快速开始（本地运行）

### 环境要求

- **JDK** 17+
- **Maven** 3.9+
- **Node.js** 18+
- **MySQL** 8.x
- **Redis** 3.0+

### 第一步：启动 MySQL 和 Redis

确保 MySQL 和 Redis 服务已在本地运行。

### 第二步：初始化数据库

> ⚠️ Windows 下如果 SQL 文件为 UTF-8 编码，需加 `--default-character-set=utf8mb4` 参数

```bash
# 创建数据库
mysql -u root -pHospital@2026 --default-character-set=utf8mb4 < sql/01_init_database.sql

# 创建表结构
mysql -u root -pHospital@2026 --default-character-set=utf8mb4 hospital_db < sql/02_create_tables.sql

# 导入初始数据（管理员/科室/医生/患者）
mysql -u root -pHospital@2026 --default-character-set=utf8mb4 hospital_db < sql/03_init_data.sql

# 导入测试排班和号源数据
mysql -u root -pHospital@2026 --default-character-set=utf8mb4 hospital_db < sql/04_test_data.sql
```

> 注：如果你的 MySQL root 密码不是 `Hospital@2026`，请修改 `backend/src/main/resources/application.yml` 中的 `spring.datasource.password` 字段。

### 第三步：启动后端

```bash
cd backend
mvn spring-boot:run
```

启动成功后会显示：
```
Started AppointmentApplication in x.xx seconds
Tomcat started on port 8080 (http) with context path '/api'
```

**后端地址**：`http://localhost:8080/api`

### 第四步：启动前端

```bash
cd frontend
npm install
npm run dev
```

启动成功后会显示：
```
VITE ready in xxx ms
➜ Local: http://localhost:5173/
```

**前端地址**：`http://localhost:5173`

---

## 访问地址

| 页面 | 地址 |
|------|------|
| 患者登录 | http://localhost:5173/login |
| 管理员/医生登录 | http://localhost:5173/admin-login |
| Swagger 接口文档 | http://localhost:8080/api/swagger-ui.html |

---

## 系统账号密码

### 患者端（手机号登录）

| 姓名 | 手机号 | 密码 | 身份证 |
|------|--------|------|--------|
| 张三 | 13800138000 | 123456 | 110101199001011234 |
| 李四 | 13800138001 | 123456 | 110101199202022345 |
| 王五 | 13800138002 | 123456 | 110101198805053456 |

### 管理员端

| 角色 | 账号 | 密码 | 说明 |
|------|------|------|------|
| 超级管理员 | admin | 123456 | 全系统管理权限 |
| 内科管理员 | dept_admin_nk | 123456 | 内科排班管理 |
| 外科管理员 | dept_admin_wk | 123456 | 外科排班管理 |

### 医生端

| 姓名 | 账号 | 密码 | 科室 |
|------|------|------|------|
| 张医生 | doctor_zhang | 123456 | 内科 |
| 李医生 | doctor_li | 123456 | 内科 |
| 王医生 | doctor_wang | 123456 | 外科 |

### 数据库

| 配置项 | 值 |
|--------|-----|
| 地址 | localhost:3306 |
| 数据库名 | hospital_db |
| 用户名 | root |
| 密码 | Hospital@2026 |

---

## 核心功能

### 患者端
- 手机号注册 / 登录
- 实名认证（身份证）
- 科室浏览、医生查询
- 号源日历、在线预约挂号
- 模拟支付（沙箱）
- 我的预约（查看/取消）
- 就诊人管理（代家属预约）

### 管理后台
- 数据仪表盘（ECharts 图表）
- 排班管理（创建/停诊）
- 预约订单管理
- 统计报表
- 系统用户管理

### 医生工作站
- 我的排班查看
- 今日患者列表

### 安全机制
- JWT Token 鉴权
- 角色权限控制（患者/管理员/科室管理员/医生）
- Redis + Lua 原子锁防号源超卖
- 黑名单机制（30天内取消≥3次自动限制预约）

---

## Docker 部署（可选）

```bash
docker-compose up -d
```

服务包含：mysql、redis、backend、frontend，前端通过 Nginx 代理后端 API。

---

## 接口文档

启动后端后访问：`http://localhost:8080/api/swagger-ui.html`
