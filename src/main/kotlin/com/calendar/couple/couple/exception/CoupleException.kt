package com.calendar.couple.couple.exception

sealed class CoupleException(
	message: String,
) : RuntimeException(message) {
	class InvalidInvitationCodeException(
		message: String = "유효하지 않은 초대 코드입니다",
	) : CoupleException(message)

	class SelfInvitationException(
		message: String = "자기 자신을 초대할 수 없습니다",
	) : CoupleException(message)

	class AlreadyCoupledInviterException(
		message: String = "초대한 사용자가 이미 다른 커플과 연결되어 있습니다",
	) : CoupleException(message)

	class AlreadyCoupledInviteeException(
		message: String = "이미 다른 커플과 연결되어 있습니다",
	) : CoupleException(message)
}