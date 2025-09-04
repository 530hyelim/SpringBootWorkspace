package com.kh.menu.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kh.menu.security.filter.JWTAuthenticationFilter;
import com.kh.menu.security.model.handler.OAuth2SuccessHandler;
import com.kh.menu.security.model.service.OAuth2Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	public SecurityFilterChain filterChain(
			HttpSecurity http, 
			JWTAuthenticationFilter jwtFilter,
			OAuth2Service oauth2Service,
			OAuth2SuccessHandler oauth2SuccessHandler) throws Exception {
		http
			// Cors 관련 빈 객체 등록
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			// CSRF는 SPA 어플리케이션에서 사용하지 않음
			.csrf(csrf -> csrf.disable())
			.exceptionHandling(e -> e
				.authenticationEntryPoint((req, res, ex) -> {
					// 토큰 유효시간 만료, 인증 실패 시 401 처리
					res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED");
				}).accessDeniedHandler((req, res, ex) -> {
					// 인증 실패 시 403 처리
					res.sendError(HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN");
				}))
			// 서버에서 인증상태를 관리하지 않게 하는 설정
			.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.oauth2Login(oauth -> oauth
				// 인증정보를 바탕으로 자동 회원가입
				// 요청처리 완료 후, accessToken과 refreshToken을 사용자에게 전달
				.userInfoEndpoint(u -> u.userService(oauth2Service))
				.successHandler(oauth2SuccessHandler))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/auth/login", "/auth/signup", "/auth/logout", "/auth/refresh").permitAll()
				.requestMatchers("/oauth2/**", "/login**", "/error").permitAll()
				.requestMatchers("/**").authenticated());
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
	
	//CORS 설정정보를 가진 빈 객체
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		// 허용 Origin 설정
		config.setAllowedOrigins(List.of("http://localhost:5173"));
		// 허용 메서드
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("Location", "Authorization"));
		config.setAllowCredentials(true); // 세션, 쿠키 허용. false면 http-only 쿠키 못 씀
		config.setMaxAge(3600L); // 요청정보 (쿠키에 대한) 캐싱시간
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		PasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}
	
	@Bean
	public RestTemplate restTemplate() {
		// rest API 서버끼리 통신할 때 사용하는 객체
		return new RestTemplate();
	}
}
