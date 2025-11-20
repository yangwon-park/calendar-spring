package com.calendar.couple.account.infrastructure.persistence.entity

import com.calendar.couple.common.BaseTimeEntity
import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "account")
@Suppress("ProtectedInFinal")
class AccountEntity(
	@Column(unique = true, nullable = false, length = 100)
	val email: String,
	password: String? = null,
	val name: String,
	role: String,
	val provider: String,
) : BaseTimeEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null

	@Column(nullable = true)
	var password: String? = password
		protected set

	var role: String = role
		protected set

	@Column(columnDefinition = "TINYINT(1) default 0")
	var isDeleted: Boolean = false
		protected set

	@Column(columnDefinition = "TINYINT(1) default 0")
	var isBanned: Boolean = false
		protected set

	@Column(columnDefinition = "TINYINT(1) default 0")
	var isWithdraw: Boolean = false
		protected set

	@Column(columnDefinition = "DATETIME(3)")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	var bannedAt: LocalDateTime? = null
		protected set

	@Column(columnDefinition = "DATETIME(3)")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	var withdrawnAt: LocalDateTime? = null
		protected set

	companion object {
		fun createUserAccount(
			email: String,
			name: String,
			provider: String,
		): AccountEntity =
			AccountEntity(
				email = email,
				name = name,
				role = "USER",
				provider = provider,
			)

		fun createAdminAccount(
			email: String,
			password: String,
			name: String,
		): AccountEntity =
			AccountEntity(
				email = email,
				password = password,
				name = name,
				role = "ADMIN",
				provider = "LOCAL",
			)
	}
}