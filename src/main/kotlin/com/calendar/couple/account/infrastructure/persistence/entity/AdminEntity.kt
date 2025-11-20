package com.calendar.couple.account.infrastructure.persistence.entity

import com.calendar.couple.common.BaseTimeEntity
import jakarta.persistence.*

/**
 * 관리자 Entity
 * Account와 1:1 관계
 */
@Entity
@Table(name = "admin")
@Suppress("ProtectedInFinal")
class AdminEntity(
	val accountId: Long,
	role: String,
) : BaseTimeEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null

	@Column(nullable = false, length = 20)
	var role: String = role
		protected set
}