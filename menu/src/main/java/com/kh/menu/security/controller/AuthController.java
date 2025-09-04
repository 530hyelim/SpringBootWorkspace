package com.kh.menu.security.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.menu.security.model.dto.AuthDto.AuthResult;
import com.kh.menu.security.model.dto.AuthDto.LoginRequest;
import com.kh.menu.security.model.dto.AuthDto.User;
import com.kh.menu.security.model.provider.JWTProvider;
import com.kh.menu.security.model.service.AuthService;
import com.kh.menu.security.model.service.KakaoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final KakaoService kakaoService;
	private final AuthService service;
	private final JWTProvider jwt;
	public static final String REFRESH_COOKIE = "REFRESH_TOKEN";
	/*
	 * 로그인
	 *  - 현재 db에 존재하지 않은 이메일이면 404 반환
	 *  - 프런트에서 응답상태 404인 경우 회원가입 처리
	 *  - 이메일은 존재하나 비밀번호가 틀린경우 401 상태(미인증상태: unauthorized) 반환
	 *  - 모두 성공 시 유저정보와 JWT 토큰 반환
	 */
	@PostMapping("/login")
	public ResponseEntity<AuthResult> login(@RequestBody LoginRequest req) {
		// 1) 사용자가 존재하는지 확인
		boolean exists = service.existsByEmail(req.getEmail());
		if (!exists) {
			System.out.println(ResponseEntity.notFound().build());
			return ResponseEntity.notFound().build(); 
		}
		try {
			AuthResult result = service.login(req.getEmail(), req.getPassword());
			// refreshToken은 http-only 쿠키로 설정하여 반환
			ResponseCookie refreshCookie = ResponseCookie
					.from(REFRESH_COOKIE, result.getRefreshToken())
					.httpOnly(true) // 자바스크립트 소스코드로 제어 X (XSS 공격 차단)
					.secure(false) // true : https에서만 사용. false : http에서도 사용가능
					.path("/") // 쿠키의 저장 위치. 최상위 경로에 저장해서 다른 모든 하위경로에서도 쓸수있게
					.sameSite("Lax") // ip주소가 서로 다른경우 non? 같은경우 Lax 설정
					.maxAge(Duration.ofDays(7)) // 만료기간 변수로 저장 안했으니까 하드코딩
					.build();
			return ResponseEntity.ok()
					.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
					.body(result);
		} catch(BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}
	
	/*
	 * 회원가입
	 */
	@PostMapping("/signup")
	public ResponseEntity<AuthResult> signup(@RequestBody LoginRequest req) {
		// 유효성검사 생략
		AuthResult result = service.signUp(req.getEmail(), req.getPassword());
		ResponseCookie refreshCookie = ResponseCookie
				.from(REFRESH_COOKIE, result.getRefreshToken())
				.httpOnly(true) // 자바스크립트 소스코드로 제어 X (XSS 공격 차단)
				.secure(false) // true : https에서만 사용. false : http에서도 사용가능
				.path("/") // 쿠키의 저장 위치. 최상위 경로에 저장해서 다른 모든 하위경로에서도 쓸수있게
				.sameSite("Lax") // ip주소가 서로 다른경우 non? 같은경우 Lax 설정
				.maxAge(Duration.ofDays(7)) // 만료기간 변수로 저장 안했으니까 하드코딩
				.build();
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(result);
	}
	
	// accessToken 재발급 엔드포인트
	@PostMapping("/refresh")
	public ResponseEntity<AuthResult> refresh(
			@CookieValue(name = REFRESH_COOKIE, required = false)
			String refreshCookie
			) {
		if (refreshCookie == null || refreshCookie.isBlank()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 쿠기도 만료
		}
		// 쿠키가 있으면 쿠키를 검증하여 새로운 accessToken 생성. refreshToken 재발급 X6
		AuthResult result = service.refreshByCookie(refreshCookie);
		return ResponseEntity.ok(result);
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		// 1. 클라이언트의 헤더에서 id값 추출
		String accessToken = resolveAccessToken(request);
		long userId = jwt.getUserId(accessToken);
		// 2. db에서 사용자의 카카오 access token 조회
		String kakaoAccessToken = service.getKakaoAccessToken(userId);
		if (kakaoAccessToken != null) {
			// 카카오에 로그아웃 요청 (액세스토큰 만료)
			kakaoService.logout(kakaoAccessToken);
		}
		// 리프레쉬토큰 제거
		ResponseCookie refreshCookie = ResponseCookie
				.from(REFRESH_COOKIE, "")
				.httpOnly(true)
				.secure(false)
				.path("/")
				.sameSite("Lax")
				.maxAge(0)
				.build();
		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.build();
	}
	
	@GetMapping("/me")
	public ResponseEntity<User> getUserInfo(HttpServletRequest req) {
		// 1. 요청 헤더에서 jwt 토큰 추출
		String jwtToken = resolveAccessToken(req); // 원래 필터에서 해주는 작업
		if (jwtToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
		}
		// 2. JWT 토큰에서 ID값 추출
		long userId = jwt.getUserId(jwtToken);
		// 3. DB에서 사용자 정보 조회
		User user = service.findUserByUserId(userId);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}
		// 만약 db에 사용자 정보 저장 안하면 카카오에 다시 요청보내서 받아와야 함
		return ResponseEntity.ok(user);
	}
	
	public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
