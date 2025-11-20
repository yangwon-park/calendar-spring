package com.calendar.couple.account.domain.enums

/**
 * 관리자 역할
 */
enum class AdminRole(
	val description: String,
) {
	/**
	 * 전체 관리자
	 * - 모든 권한 보유
	 * - 관리자 계정 생성/삭제 가능
	 * - 시스템 설정 변경 가능
	 */
	SUPER_ADMIN("전체 관리자"),

	/**
	 * 일반 관리자
	 * - 일반적인 관리 업무
	 * - 회원 관리, 상품 승인, 신고 처리 등
	 */
	ADMIN("일반 관리자"),
}