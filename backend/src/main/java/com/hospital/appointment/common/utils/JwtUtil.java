package com.hospital.appointment.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

/**
 * 生成JWT令牌的方法
 * @param userId 用户ID
 * @param username 用户名
 * @return 生成的JWT令牌字符串
 */
    public String generateToken(Long userId, String username, String role) {
    // 创建一个Map来存储JWT的声明（claims）
        Map<String, Object> claims = new HashMap<>();
          claims.put("userId", userId);
          claims.put("role", role);

    // 使用Jwts.builder()构建JWT令牌
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
            // 设置过期时间（当前时间加上过期时长）
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
            // 使用HS256算法和密钥进行签名
                .signWith(SignatureAlgorithm.HS256, secret)
            // compact()方法生成最终的JWT令牌字符串
                .compact();
    }

/**
 * 解析JWT令牌的方法
 * @param token 需要解析的JWT令牌字符串
 * @return 返回解析后的Claims对象，包含令牌中的声明信息
 */
    public Claims parseToken(String token) {
        // 检查令牌是否为空且是否以"Bearer "开头
        if (token != null && token.startsWith("Bearer ")) {
            // 如果是Bearer令牌，则去掉前缀"Bearer "
            token = token.substring(7);
        }
        // 使用Jwts解析器设置签名密钥并解析令牌，返回令牌的主体部分(Claims)
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

/**
 * 验证令牌(token)的有效性
 * @param token 需要验证的令牌字符串
 * @return 如果令牌有效返回true，否则返回false
 */
    public boolean validateToken(String token) {
        try {
        // 尝试解析令牌
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }
}
