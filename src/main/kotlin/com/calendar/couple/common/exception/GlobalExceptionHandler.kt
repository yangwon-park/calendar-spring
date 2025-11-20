package com.calendar.couple.common.exception

import com.calendar.couple.auth.exception.AuthException
import com.calendar.couple.common.dto.CommonErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
	@ExceptionHandler(AuthException.AccountNotFoundException::class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	fun handleAccountNotFoundException(e: AuthException.AccountNotFoundException): CommonErrorResponse =
		CommonErrorResponse(e.message, ErrorCode.ACCOUNT_NOT_FOUND.status)

	@ExceptionHandler(MethodArgumentNotValidException::class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	fun handleValidationExceptions(e: MethodArgumentNotValidException): CommonErrorResponse =
		CommonErrorResponse(e.message, HttpStatus.BAD_REQUEST.value())

	@ExceptionHandler(HttpMessageNotReadableException::class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): CommonErrorResponse =
		CommonErrorResponse(e.message, HttpStatus.BAD_REQUEST.value())

	@ExceptionHandler(IllegalStateException::class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	fun handleIllegalStateException(e: IllegalStateException): CommonErrorResponse =
		CommonErrorResponse(e.message, HttpStatus.INTERNAL_SERVER_ERROR.value())
}