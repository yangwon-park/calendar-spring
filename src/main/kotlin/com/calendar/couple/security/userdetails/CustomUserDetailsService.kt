package com.calendar.couple.security.userdetails

import com.calendar.couple.account.infrastructure.AccountMapper.toDomain
import com.calendar.couple.account.infrastructure.persistence.repository.AccountRepository
import com.calendar.couple.security.userdetails.dto.UserDetailsDto
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.lang.IllegalStateException

private val log = KotlinLogging.logger {}

@Service
class CustomUserDetailsService(
	private val accountRepository: AccountRepository,
) : UserDetailsService {
	override fun loadUserByUsername(username: String): UserDetails {
		val account =
			accountRepository.findByEmail(username)?.toDomain()
				?: throw UsernameNotFoundException("사용자를 찾을 수 없습니다. Email: $username")

		return CustomUserDetails(
			UserDetailsDto(
				account.id ?: throw IllegalStateException(),
				account.email,
				account.password,
				account.role.name,
				account.isBanned,
				account.isDeleted,
				account.isWithdraw,
			),
		)
	}

	fun loadUserByAccountId(accountId: Long): CustomUserDetails {
		val account =
			accountRepository.findByIdOrNull(accountId)?.toDomain()
				?: throw UsernameNotFoundException("사용자를 찾을 수 없습니다. AccountId: $accountId")

		return CustomUserDetails(
			UserDetailsDto(
				account.id ?: throw IllegalStateException(),
				account.email,
				account.password,
				account.role.name,
				account.isBanned,
				account.isDeleted,
				account.isWithdraw,
			),
		)
	}
}