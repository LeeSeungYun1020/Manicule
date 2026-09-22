package com.leeseungyun1020.manicule.core.domain.record

data class AddRecordResult(
    val recordId: Long,
    val shouldCheckFinish: Boolean,
    val maxEndPage: Int,
)
