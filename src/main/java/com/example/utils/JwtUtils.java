package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {
    @Value("${spring.security.jwt.secret}")
    String key;
    @Value("${spring.security.jwt.expire}")
    int expire;
    @Resource
    StringRedisTemplate template;

    public UserDetails  toUserInfo(DecodedJWT decodedJWT){
        Map<String, Claim> claims = decodedJWT.getClaims();
        return User.withUsername(claims.get("username").asString())
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();

    }
    public int toUserId(DecodedJWT decodedJWT){
        Map<String, Claim> claims = decodedJWT.getClaims();
        return claims.get("id").asInt();
    }

    public boolean invalidateJwt(String headerToken){
        String token = validateHeaderToken(headerToken);
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier= JWT.require(algorithm).build();
        try{
            DecodedJWT verify = jwtVerifier.verify(token);
           String  uuid = verify.getId();
           return deleteToken(uuid,verify.getExpiresAt());
        }catch (JWTVerificationException e){
            return false;
        }
    }
    public  boolean deleteToken(String uuid,Date date){
        if(isTimeoutToken(uuid)){return false;}
        long time = Math.max(date.getTime() - System.currentTimeMillis(),0);
        template.opsForValue()
                .set(Const.TOKEN_BLACKLIST_PREFIX + uuid,"",time, TimeUnit.MILLISECONDS);
        return  true;
    }
    public boolean isTimeoutToken(String uuid){
        return Boolean.TRUE.equals(template.hasKey(Const.TOKEN_BLACKLIST_PREFIX + uuid));
    }

    public DecodedJWT resolveJwtToken(String headerToken) {
        String token = validateHeaderToken(headerToken);
        if(token==null){return null;}
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier= JWT.require(algorithm).build();
        try{
            DecodedJWT verified = jwtVerifier.verify(token);
            if(isTimeoutToken(verified.getId())) {return null;}
            Date expireTime=verified.getExpiresAt();
            return new Date().after(expireTime)? null :verified;
        }catch (JWTVerificationException e){
            return null;
        }
    }
    public String validateHeaderToken(String headerToken) {
        if(headerToken==null || !headerToken.startsWith("Bearer ")){
            return null;
        }
        return  headerToken.substring(7);
    }
public String generateJwtToken(UserDetails userDetails,int id,String username) {
    Algorithm algorithm = Algorithm.HMAC256(key);
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", id)
                .withClaim("username", username)
                .withClaim("authorities", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(this.expireTime())
                .withIssuedAt(new Date())
                .sign(algorithm);
    }
    public Date expireTime(){
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.HOUR,expire*24);
        return  instance.getTime();
    }
}
