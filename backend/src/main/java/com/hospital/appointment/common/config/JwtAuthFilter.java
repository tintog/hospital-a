package com.hospital.appointment.common.config;

import com.hospital.appointment.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 用于处理HTTP请求中的JWT令牌，验证用户身份并设置Spring Security安全上下文
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil; // JWT工具类，用于处理令牌的解析和验证

    /**
     * 执行过滤操作的核心方法
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求中解析JWT令牌
        String token = resolveToken(request);

        // 如果令牌存在且有效，则进行用户认证
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            try {
                // 解析令牌获取用户信息
                Claims claims = jwtUtil.parseToken(token);
                Long userId = claims.get("userId", Long.class); // 获取用户ID
                String username = claims.getSubject(); // 获取用户名
                String role = claims.get("role", String.class); // 获取用户角色

                // 创建用户权限
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                // 创建认证令牌
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, username,
                                Collections.singletonList(authority));

                // 将认证信息设置到安全上下文中
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // 如果令牌验证过程中出现异常，清除安全上下文
                SecurityContextHolder.clearContext();
            }
        }

        // 继续过滤器链的执行
        filterChain.doFilter(request, response);
    }

    /**
     * 从HTTP请求中解析JWT令牌
     * @param request HTTP请求
     * @return JWT令牌字符串，如果没有找到则返回null
     */
    private String resolveToken(HttpServletRequest request) {
        // 从请求头中获取Authorization字段
        String bearer = request.getHeader("Authorization");
        // 检查是否存在Bearer类型的令牌
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            // 返回去掉"Bearer "前缀的令牌
            return bearer.substring(7);
        }
        return null;
    }
}
