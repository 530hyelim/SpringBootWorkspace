package com.kh.menu.security.model.provider;

import java.security.Key; // 암호화 서명이 완료된 키 객체만 보관
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/*
 * #1. JWT
 *  - JSON 형식의 데이터를 서명을 통해 위변조를 방지한 토큰으로, 인증 및 인가에 사용한다.
 *  - REST API 서버는 제약조건 상 무상태 서버로 설계되어, 사용자의 인증정보를 서버 세션에
 *    저장하지 않고, 클라이언트에게 인증정보(JWT)를 저장시킨다.
 *  - 발급된 JWT 토큰은 클라이언트가 매 요청 시 함께 전달하여, 인증에 사용한다.
 *  
 * #2. JWT 토큰을 활용한 인증/인가 메커니즘
 *  1. 사용자가 아이디와 비밀번호로 로그인 요청을 보낸다.
 *  2. 서버는 사용자 정보를 확인한 뒤, JWT 토큰을 생성하여 클라이언트에게 전달한다.
 *  3. 클라이언트는 이 토큰을 LocalStorage 혹은 Cookie에 저장한다.
 *  4. 이후 API 요청 시, 클라이언트는 요청 헤더에 토큰을 포함하여 전송한다.
 *  5. 서버는 토큰의 서명과 만료시간을 검증하여 유효하다면 요청을 처리한다.
 *  6. 토큰이 만료된 경우, 클라이언트는 재 로그인을 통해 토큰을 재발급한다.
 *  
 * #3. JWT 토큰 구조
 *  - 헤더 : 토큰의 타입과 서명에 사용한 알고리즘
 *  - 페이로드 : 토큰의 내용(클레임)이 포함된다.
 *            내용으로는 sub(사용자 id), exp(만료시간), etc 등이 포함된다.
 *  - 서명 : 헤더와 페이로드를 조합하여 암호화한 값
 *  
 * #4. 주의사항
 *  - 토큰은 브라우저에 저장되므로, 토큰 탈취 위험이 존재한다. 탈취 당하는 경우를 대비해
 *    만료시간은 짧게 설정하는게 좋다.
 *  - 따라서 토큰의 페이로드에는 민감한 개인정보를 저장하면 안된다.
 */

@Component
public class JWTProvider {
	private final Key key;
	
	public JWTProvider(
			@Value("${jwt.secret}")
			String secretBase64) { // 토큰 서명에 사용할 인코딩 된 키 값
		byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}
	
	public String createAccessToken(long id, int minutes) {
		Date now = new Date();
		return Jwts.builder()
				.setSubject(String.valueOf(id)) // 페이로드에 저장할 id
				.setIssuedAt(now) // 토큰 발행시간
				.setExpiration(new Date(now.getTime() + (1000 * 60 * minutes))) // 만료 시간
				.signWith(key, SignatureAlgorithm.HS256) // 서명에 사용할 키 값과 알고리즘
				.compact();
	}
}
