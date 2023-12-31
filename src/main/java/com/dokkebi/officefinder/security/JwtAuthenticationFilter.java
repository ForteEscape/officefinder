package com.dokkebi.officefinder.security;

import javax.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
//@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends
    OncePerRequestFilter { //OncePerRequestFilter는 요청이 올때마다 이 필터가 실행됨.

  private final String TOKEN_HEADER = "Authorization";

  private final TokenProvider tokenProvider;

  /*
      요청이 올때마다 실행되는 Filter, 토큰을 검증하고, 검증된 유저를 등록하는 과정
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String token = resolveTokenFromRequest(request); // 요청 헤더에서 jwt 토큰 가져오기
    //String token = resolveTokenFromCookie(request);

    if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) { //토큰이 시간만료되었는지 체크
      Authentication auth = tokenProvider.getAuthentication(token); // 사용자와 사용자권한정보가 포함된 인증토큰 리턴
      SecurityContextHolder.getContext().setAuthentication(auth);

      log.info(String.format("[%s_%s] -> %s", tokenProvider.getUserType(token),
          tokenProvider.getUserId(token), request.getRequestURI()));
    }

    filterChain.doFilter(request, response);
  }

  /*
      헤더에서 jwt토큰을 추출하는 메서드
   */
  private String resolveTokenFromRequest(HttpServletRequest request) {
    return tokenProvider.resolveTokenFromHeader(request.getHeader(TOKEN_HEADER));
  }

  private String resolveTokenFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    } else {
      for (Cookie cookie : cookies) {
        if ("Authorization".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

}

