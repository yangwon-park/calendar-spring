package com.calendar.couple.common.config

import com.calendar.couple.common.properties.CorsProperties
import com.calendar.couple.security.jwt.JwtAuthenticationEntryPoint
import com.calendar.couple.security.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
	private val corsProperties: CorsProperties,
	private val jwtAuthenticationFilter: JwtAuthenticationFilter,
	private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
) {
	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
		http
			.csrf { it.disable() }
			.logout { it.disable() }
			.formLogin { it.disable() }
			.httpBasic { it.disable() }
			.cors { it.configurationSource(corsConfigurationSource()) }
			.sessionManagement {
				it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			}.exceptionHandling {
				it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
			}.authorizeHttpRequests {
				// AuthorizationFilter 사용
				it
					.requestMatchers(
						"/",
						"/actuator/**",
						"/swagger-ui/**",
						"/api/auth/sign-in",
						"/api/auth/refresh",
						"/api/account/sign-up",
					).permitAll()
					.anyRequest()
					.authenticated()
			}.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
			.build()

	@Bean
	fun corsConfigurationSource(): CorsConfigurationSource {
		val config =
			CorsConfiguration().apply {
				allowedOrigins = corsProperties.allowedOrigins
				allowedHeaders = corsProperties.allowedHeaders
				allowedMethods = corsProperties.allowedMethods
				allowCredentials = corsProperties.allowCredentials
			}

		return UrlBasedCorsConfigurationSource().apply {
			registerCorsConfiguration("/**", config)
		}
	}

	@Bean
	fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

	@Bean
	fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
		authenticationConfiguration.authenticationManager
}