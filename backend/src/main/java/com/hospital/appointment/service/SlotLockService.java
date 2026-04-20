package com.hospital.appointment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
public class SlotLockService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${appointment.slot-lock-timeout}")
    private Integer lockTimeoutSeconds;

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

    public void initSlotInRedis(Long slotId) {
        String slotKey = "slot:info:" + slotId;
        redisTemplate.opsForHash().put(slotKey, "status", "0");
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void cleanupExpiredLocks() {
        log.debug("执行过期号源锁清理任务...");
    }
}
