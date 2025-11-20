package com.calendar.couple.account.domain.enums

enum class AccountRole(
	val description: String,
) {
	USER("일반 회원"),
	ILLUSTRATOR("일러스트레이터"),
	ARTIST("아티스트"),
	ADMIN("관리자"),
}