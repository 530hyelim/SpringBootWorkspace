package com.kh.menu.security.model.service;

import java.util.List;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.menu.security.model.dao.AuthDao;
import com.kh.menu.security.model.dto.AuthDto.AuthResult;
import com.kh.menu.security.model.dto.AuthDto.User;
import com.kh.menu.security.model.dto.AuthDto.UserAuthority;
import com.kh.menu.security.model.dto.AuthDto.UserCredential;
import com.kh.menu.security.model.provider.JWTProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final AuthDao authDao;
	private final PasswordEncoder encoder; // bean 객체 생성 필요
	private final JWTProvider jwt;
	
	public boolean existsByEmail(String email) {
		User user = authDao.findUserByEmail(email);
		return user != null;
	}

	public AuthResult login(String email, String password) {
		// 1. 사용자 정보 조회
		User user = authDao.findUserByEmail(email);
		if(!encoder.matches(password, user.getPassword())) {
			throw new BadCredentialsException("비밀번호 오류");
		}
		// 2. 토큰 발급
		String accessToken = jwt.createAccessToken(user.getId(), 30); // payload의 sub값(id)
		String refreshToken = jwt.createRefreshToken(user.getId(), 7);
		User userNoPassword = User.builder()
								.id(user.getId())
								.email(user.getEmail())
								.name(user.getName())
								.profile(user.getProfile())
								.roles(user.getRoles())
								.build();
		return AuthResult.builder()
						.accessToken(accessToken)
						.refreshToken(refreshToken)
						.user(userNoPassword)
						.build();
	}

	@Transactional
	public AuthResult signUp(String email, String password) {
		// 1. Users 테이블에 데이터 추가
		User user = User.builder()
						.email(email)
						.name(email.split("@")[0])
						.build();
		authDao.insertUser(user);
		// 2. Credential 추가
		UserCredential cred = UserCredential.builder()
						.userId(user.getId())
						.password(encoder.encode(password))
						.build();
		authDao.insertCred(cred);
		// 3. 권한 추가
		UserAuthority auth = UserAuthority.builder()
						.userId(user.getId())
						.roles(List.of("ROLE_USER"))
						.build();
		authDao.insertUserRole(auth);
		// 4. 토큰 발급
		String accessToken = jwt.createAccessToken(user.getId(), 30); // 30분
		String refreshToken = jwt.createRefreshToken(user.getId(), 7); // 7일
		user = authDao.findUserByUserId(user.getId());
		user.setPassword(null); // 비밀번호 제외 필요
		return AuthResult.builder()
						.accessToken(accessToken)
						.refreshToken(refreshToken)
						.user(user)
						.build();
	}
}
