package com.example.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LimitUtils {
    @Resource
    StringRedisTemplate Template;
    public Boolean emailLimited(String key,int codeTime){
        if(Boolean.TRUE.equals(Template.hasKey(key))){
            return true;
        }
        else{
            Template.opsForValue().set(key,"",codeTime, TimeUnit.SECONDS);
            return false;
        }
    }
}
