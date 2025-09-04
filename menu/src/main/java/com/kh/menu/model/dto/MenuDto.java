package com.kh.menu.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MenuDto {
	/*
	 * DTO
	 *  - REST API와 같은 서버에서는 클라이언트-서버 간에 단순히 데이터만 주고받는 경우가 많다.
	 *    이런 경우, DTO 클래스 형태로 필요한 최소한의 데이터만 포장하여 전달하는게 권장된다.
	 */
	// 메뉴 응답용 dto
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MenuResponse {
		//@Positive(message="메뉴 id는 양수입니다.")
		//@NotBlank(message="필수입니다.")
		@Schema(description="메뉴 id", example="1")
		private long id;
		
		@Schema(description="식당명", example="경민통닭")
		private String restaurant;
		private String name;
		
		@Schema(description="가격(원)", example="10000", minimum="0")
		private int price;
		
		@Schema(description="메뉴타입 (kr, jp, ch)", example="kr",
				allowableValues = {"kr", "jp", "ch"}) // 문자열보다는 이넘 타입으로 관리하는게 맞음
		private String type;
		private String taste;
	}
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MenuPost {
		private long id; // 값을 전달받은 프론트에서 리디렉션 할 수 있도록 반환
		private String restaurant;
		private String name;
		private int price;
		private String type;
		private String taste;
	}
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MenuPut {
		private long id;
		private String restaurant;
		private String name;
		private int price;
		private String type;
		private String taste;
	}
}
