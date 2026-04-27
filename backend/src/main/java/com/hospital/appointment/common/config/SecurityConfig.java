package com.hospital.appointment.common.config;

import com.hospital.appointment.common.config.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

/**
 * 配置Spring Security的安全过滤器链，用于定义应用程序的安全策略
 * @param http HttpSecurity对象，用于构建安全配置
 * @return 配置好的SecurityFilterChain实例
 * @throws Exception 可能抛出的异常
 */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // 配置CORS策略，使用自定义的corsConfigurationSource作为配置源
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // 禁用CSRF保护，因为这是一个无状态API
            .csrf(csrf -> csrf.disable())
        // 设置会话创建策略为STATELESS，不创建会话
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // 配置HTTP请求授权规则
            .authorizeHttpRequests(auth -> auth
            // 允许任何人访问的公共端点
                .requestMatchers(
                    "/auth/**",          // 认证相关接口
                    "/department/list",  // 科室列表接口
                    "/doctor/list",     // 医生列表接口
                    "/doctor/{id}",     // 医生详情接口
                    "/slot/calendar",   // 排班日历接口
                    "/swagger-ui/**",   // Swagger UI相关接口
                    "/v3/api-docs/**",  // API文档接口
                    "/swagger-ui.html", // Swagger UI首页
                    "/error"            // 错误处理接口
                ).permitAll()
            // 需要ADMIN或DEPT_ADMIN角色才能访问的管理接口
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "DEPT_ADMIN")
            // 需要DOCTOR角色才能访问的医生相关接口
                .requestMatchers(
                    "/doctor/my-schedule",        // 医生排班查询
                    "/doctor/today-patients",     // 医生今日患者列表
                    "/doctor/profile",            // 医生个人信息
                    "/doctor/profile/phone",      // 医生手机号修改
                    "/doctor/profile/password"   // 医生密码修改
                ).hasRole("DOCTOR")
            // 所有其他请求都需要认证
                .anyRequest().authenticated()
            )
        // 配置异常处理
            .exceptionHandling(ex -> ex
            // 认证入口点处理，处理未认证的请求
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\",\"data\":null}");
                })
            // 访问拒绝处理器，处理权限不足的请求
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(403);
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足\",\"data\":null}");
                })
            )
        // 添加JWT认证过滤器，在用户名密码认证过滤器之前执行
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    // 构建并返回SecurityFilterChain实例
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
