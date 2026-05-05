package com.hospital.appointment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

/**
 * 号源锁服务类
 * 用于处理号源的锁定、解锁和状态管理
 */
@Service
@Slf4j
public class SlotLockService {

    @Autowired
    private StringRedisTemplate redisTemplate; // Redis操作模板

    @Value("${appointment.slot-lock-timeout}")
    private Integer lockTimeoutSeconds; // 号源锁超时时间，单位秒

    /**
     * Lua脚本，用于锁定号源
     * 1. 检查号源状态是否为0（可预约）
     * 2. 尝试设置锁
     * 3. 如果设置成功，更新号源状态为1（已锁定）
     * 返回值：
     * -1: 号源状态不为0
     * 0: 设置锁失败
     * 1: 设置锁成功
     */
    private static final String LOCK_SCRIPT = """
        local slot_key = 'slot:info:' .. ARGV[1]
        local lock_key = 'slot:lock:' .. ARGV[1]
        
        local status = redis.call('HGET', slot_key, 'status')
        if status and status ~= '0' then
            return -1
        end
        
        local locked = redis.call('SET', lock_key, ARGV[2], 'NX', 'EX', ARGV[3])
        if not locked then
            return 0
        end
        
        redis.call('HSET', slot_key, 'status', '1')
        redis.call('HSET', slot_key, 'locked_by', ARGV[2])
        redis.call('HSET', slot_key, 'locked_expire', ARGV[4])
        
        return 1
        """;

    /**
     * 锁定号源
     * @param slotId 号源ID
     * @param patientId 患者ID
     * @return 返回结果：-1表示号源不可用，0表示锁定失败，1表示锁定成功
     */
    public Long lockSlot(Long slotId, Long patientId) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LOCK_SCRIPT, Long.class);
        Long result = redisTemplate.execute(
                script,
                Collections.emptyList(),
                slotId.toString(),
                patientId.toString(),
                String.valueOf(lockTimeoutSeconds),
                String.valueOf(System.currentTimeMillis() + lockTimeoutSeconds * 1000L)
        );
        return result != null ? result : -1L;
    }

    /**
     * 解锁号源
     * @param slotId 号源ID
     * @param patientId 患者ID
     * @return 解锁成功返回true，失败返回false
     */
    public boolean unlockSlot(Long slotId, Long patientId) {
        String unlockScript = """
            local lock_key = 'slot:lock:' .. ARGV[1]
            local slot_key = 'slot:info:' .. ARGV[1]
            local patient_id = ARGV[2]
            
            local current = redis.call('GET', lock_key)
            if current == patient_id then
                redis.call('DEL', lock_key)
                redis.call('HSET', slot_key, 'status', '0')
                redis.call('HDEL', slot_key, 'locked_by', 'locked_expire')
                return 1
            end
            return 0
            """;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(unlockScript, Long.class);
        Long result = redisTemplate.execute(script, Collections.emptyList(),
                slotId.toString(), patientId.toString());
        return result != null && result == 1;
    }

    /**
     * 初始化号源信息到Redis
     * @param slotId 号源ID
     */
    public void initSlotInRedis(Long slotId) {
        String slotKey = "slot:info:" + slotId;
        redisTemplate.opsForHash().put(slotKey, "status", "0");
    }

    /**
     * 定时清理过期的号源锁
     * 每隔5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void cleanupExpiredLocks() {
        log.debug("执行过期号源锁清理任务...");
        Set<String> slotKeys = redisTemplate.keys("slot:info:*");
        if (slotKeys == null || slotKeys.isEmpty()) {
            return;
        }

        int cleaned = 0;
        for (String slotKey : slotKeys) {
            Object status = redisTemplate.opsForHash().get(slotKey, "status");
            if (!"1".equals(String.valueOf(status))) {
                continue;
            }

            String slotId = slotKey.substring("slot:info:".length());
            String lockKey = "slot:lock:" + slotId;
            Boolean exists = redisTemplate.hasKey(lockKey);
            if (Boolean.FALSE.equals(exists)) {
                redisTemplate.opsForHash().put(slotKey, "status", "0");
                redisTemplate.opsForHash().delete(slotKey, "locked_by", "locked_expire");
                cleaned++;
            }
        }

        if (cleaned > 0) {
            log.info("过期号源锁清理完成，恢复{}个号源为可预约", cleaned);
        }
    }
}
