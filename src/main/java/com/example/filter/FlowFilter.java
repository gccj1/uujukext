package com.example.filter;

import com.example.entity.RestBean;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Order(-101)
public class FlowFilter extends HttpFilter {
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(isFlow(request.getRemoteAddr())){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(RestBean.failure(403,"操作过于频繁").toJSONString());
        }
        else  chain.doFilter(request, response);
    }
    boolean isFlow(String ip) {
        synchronized (ip.intern()){
            if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(Const.FLOW_LIMIT_IP+ ip))){
                return true;
            }
            return counterLimit(ip);
        }
     }
     boolean counterLimit(String ip) {
         if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(Const.FLOW_LIMIT_COUNT + ip))){
             long increment = Optional.ofNullable(stringRedisTemplate.opsForValue().increment(Const.FLOW_LIMIT_COUNT + ip)).orElse(0L);
             if(increment >= 10){
                 stringRedisTemplate.opsForValue().set(Const.FLOW_LIMIT_IP+ ip, "blocked", 30, TimeUnit.SECONDS);
             }
             return increment >=10;
         }
         else{
             stringRedisTemplate.opsForValue().set(Const.FLOW_LIMIT_COUNT + ip, "1", 3, TimeUnit.SECONDS);
             return false;
         }
     }
}
