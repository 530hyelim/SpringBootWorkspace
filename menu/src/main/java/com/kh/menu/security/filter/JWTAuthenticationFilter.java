package com.kh.menu.security.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kh.menu.security.model.provider.JWTProvider;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
	private final JWTProvider jwt;
	
	// accessToken 인증 확인용 필터
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// 1) 요청 header에서 Authorization 추출
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			try {
				// 2) 토큰에서 userId 추출
				String token = header.substring(7).trim();
				long userId = jwt.getUserId(token);
				log.debug("userId : {}", userId);
				
				UsernamePasswordAuthenticationToken authToken
				= new UsernamePasswordAuthenticationToken(userId, null,
					List.of(new SimpleGrantedAuthority("ROLE_USER"))
					// payload에 롤정보 같이보내거나
					// db에서 가져오거나 (하드드라이브에 저장돼서 성능이 안좋음)
					// 실무? inMemoryDB 메모리 내부에 db 설치해서 유저정보 담아서 select 해옴 x1000
				);
				// 인증처리 끗.
				SecurityContextHolder.getContext().setAuthentication(authToken);
			} catch(ExpiredJwtException e) {
				SecurityContextHolder.clearContext(); // 인증정보 지우기
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태
				return;
			}
		}
		filterChain.doFilter(request, response);
	}
	
}
