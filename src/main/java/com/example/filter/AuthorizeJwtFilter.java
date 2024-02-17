package com.example.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class AuthorizeJwtFilter extends OncePerRequestFilter {
    @Resource
    JwtUtils jwtUtils;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String  headerToken = request.getHeader("Authorization");
        DecodedJWT decodedJWT=jwtUtils.resolveJwtToken(headerToken);
        if(decodedJWT != null){
            UserDetails user= jwtUtils.toUserInfo(decodedJWT);
            System.out.println(user.getAuthorities());
            UsernamePasswordAuthenticationToken authentication=
                    new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
            authentication
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("id",jwtUtils.toUserId(decodedJWT));
        }
        filterChain.doFilter(request,response);
    }
}
