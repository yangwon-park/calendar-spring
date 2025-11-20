package com.calendar.couple.security.userdetails

import com.calendar.couple.security.userdetails.dto.UserDetailsDto
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
	private val userDetailsDto: UserDetailsDto,
) : UserDetails {
	val accountId: Long
		get() = userDetailsDto.id

	val roleType: String
		get() = userDetailsDto.role

	override fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority(userDetailsDto.role))

	override fun getPassword(): String? = userDetailsDto.password

	override fun getUsername(): String = userDetailsDto.email

	override fun isAccountNonExpired(): Boolean = true

	override fun isAccountNonLocked(): Boolean = !userDetailsDto.isBanned

	override fun isCredentialsNonExpired(): Boolean = true

	override fun isEnabled(): Boolean = !userDetailsDto.isDeleted && !userDetailsDto.isWithdraw
}