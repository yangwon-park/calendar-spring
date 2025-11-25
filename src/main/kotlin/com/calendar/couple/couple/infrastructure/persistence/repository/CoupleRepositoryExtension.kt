package com.calendar.couple.couple.infrastructure.persistence.repository

import com.calendar.couple.couple.infrastructure.persistence.entity.CoupleEntity

/**
 * 특정 계정이 포함된 커플 정보를 조회합니다.
 * account1_id 또는 account2_id가 일치하는 커플을 찾습니다.
 */
fun CoupleRepository.findByAccountId(accountId: Long): CoupleEntity? =
	findByAccount1IdOrAccount2Id(accountId, accountId)

/**
 * 특정 계정이 커플로 연결되어 있는지 확인합니다.
 */
fun CoupleRepository.existsByAccountId(accountId: Long): Boolean = 
	existsByAccount1IdOrAccount2Id(accountId, accountId)

fun CoupleRepository.deleteByAccountId(accountId: Long) = 
	deleteByAccount1IdOrAccount2Id(accountId, accountId)