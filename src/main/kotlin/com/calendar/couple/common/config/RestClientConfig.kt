package com.calendar.couple.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {
	@Bean
	fun restClient(): RestClient =
		RestClient
			.builder()
			.build()

	// TODO: 필요시 추가 설정
	// - timeout 설정
	// - interceptor 설정
	// - error handler 설정
}