package com.dokkebi.officefinder.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { //OncePerRequestFilter는 요청이 올때마다 이 필터가 실행됨.
    @Value("${spring.jwt.token-header}")
    private String TOKEN_HEADER;

    private final TokenProvider tokenProvider;

    /*
        요청이 올때마다 실행되는 Filter, 토큰을 검증하고, 검증된 토큰을 등록하는 과정
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveTokenFromRequest(request); // 요청 헤더에서 jwt 토큰 가져오기

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) { //토큰이 시간만료되었는지 체크
            Authentication auth = tokenProvider.getAuthentication(token); // 사용자와 사용자권한정보가 포함된 인증토큰 리턴
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.info(String.format("[%s] -> %s",tokenProvider.getUserNameAndType(token), request.getRequestURI()));
        }

        filterChain.doFilter(request, response);
    }

    /*
        헤더에서 jwt토큰을 추출하는 메서드
     */
    private String resolveTokenFromRequest(HttpServletRequest request) {
        return tokenProvider.resolveTokenFromHeader(request.getHeader(TOKEN_HEADER));
    }
}