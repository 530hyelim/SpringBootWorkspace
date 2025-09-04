package com.kh.menu.security.model.dto;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

public class CustomOAuth2User extends DefaultOAuth2User {
	private final long userId; // Users 테이블의 pk 값
	
	public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes,
			String nameAttributeKey, long userId) {
		super(authorities, attributes, nameAttributeKey);
		this.userId = userId; // 안하면 카카오 인증 서버 id값이 pk로 전달됨
	}
	
	public long getUserId() {
		return userId;
	}
}
