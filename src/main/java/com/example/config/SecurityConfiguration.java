package com.example.config;

import com.example.Service.AccountService;
import com.example.entity.DTO.Account;
import com.example.entity.VO.AuthorizeVO;
import com.example.entity.RestBean;
import com.example.filter.AuthorizeJwtFilter;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfiguration {
    @Resource
    JwtUtils jwtUtils;
    @Resource
    AuthorizeJwtFilter jwtFilter;

    @Resource
    AccountService service;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf -> conf
                        .requestMatchers("/api/auth/**","/error").permitAll()
                        .anyRequest().authenticated() )
                .formLogin(conf ->conf
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("username")
                        .successHandler(this::onAuthenticationSuccess)
                        .failureHandler(this::onAuthenticationFailure))
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onLogoutSuccess) )
                .exceptionHandling(conf ->conf
                        .authenticationEntryPoint(this::onExceptionHandler)
                        .accessDeniedHandler(this::onAccessDeniedHandler)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(conf ->conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        User user =(User) authentication.getPrincipal();
        Account account = service.findByUsernameOrEmail(user.getUsername());
        response.setCharacterEncoding("UTF-8");
        String jwtToken = jwtUtils.generateJwtToken(user,account.getId(), account.getUsername());
        AuthorizeVO authorizeVO =new AuthorizeVO();
        BeanUtils.copyProperties(account, authorizeVO);
        authorizeVO.setExpire(jwtUtils.expireTime());
        authorizeVO.setToken(jwtToken);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(RestBean.success(authorizeVO).toJSONString());
    }

    public void onAccessDeniedHandler(HttpServletRequest request, HttpServletResponse response, AccessDeniedException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(RestBean.failure(403,authException.getMessage()).toJSONString());
    }

        public void onExceptionHandler(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(RestBean.failure(401,authException.getMessage()).toJSONString());
        }


        public void onLogoutSuccess(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Authentication authentication) throws IOException, ServletException {
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            String header= request.getHeader("Authorization");
            if(jwtUtils.invalidateJwt(header)){
                writer.write(RestBean.success().toJSONString());
            }else {
                writer.write(RestBean.failure(400,"退出失败").toJSONString());
            }
        }



    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(RestBean.failure(401,"用户名或密码错误").toJSONString());
    }
}
