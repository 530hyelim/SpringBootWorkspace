package com.kh.menu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.menu.model.service.MenuService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController // @ResponseBody + @Controller
// 컨트롤러의 모든 메서드에 @ResponseBody를 붙여준다
public class MenuController {
	/*
	 * 1. REST(Representational State Transfer)
	 *  - 자원의 현재 상태(state)를 어떤 형식으로 전달하는 규칙, 방법
	 *    자원(Resource)?
	 *     - 서버에 존재하는 모든 데이터(html 문서, 이미지, 사용자 정보 등)을 의미한다.
	 *     - REST는 모든 자원에 대한 유니크한 URI를 설계하는 것이 원칙이다.
	 *       ex) 1번 게시글 자원 -> /board/1
	 *     - http 메서드의 전송 방식에 따라 같은 URI여도 서로 다른 자원을 가리킨다.
	 *       GET : 자원 조회
	 *       POST : 자원 생성
	 *       PUT/PATCH : 자원 수정
	 *       DELETE : 자원 삭제
	 *  - 형식(Representation)
	 *     - 자원의 상태를 표현하는 형식
	 *     - 클라이언트의 요청에 따라 /board/1과 같은 자원은 "xml, json, html"등 다양한
	 *       형식으로 표현될 수 있다.
	 *     - 자원의 형식은 content-type 혹은 request header에 선택된다.
	 *     - REST방식을 사용하면 URI와 HTTP 메서드만 확인 시 어떤 기능을 하는지 쉽게 알 수 있다.
	 *  
	 * 2. REST API
	 *  - REST 아키텍처 스타일에 따라 요청한 자원에 대한 CRUD를 진행하는 서버
	 *  - REST 설계원칙을 잘 준수 할 수록 RESTful한 API 서버라고 부름
	 *  - 일반 MVC 컨트롤러는 HTML을 반환하나, REST API는 JSON을 반환한다. 
	 */
	private final MenuService menuService;
	
	@GetMapping("/menus")
	public ResponseEntity<?> menus() {
		
		return null;
	}
}
