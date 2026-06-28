package com.leeseungyun1020.manicule.core.data.mapper

import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import com.leeseungyun1020.manicule.core.model.ReadingRecord

fun ReadingRecordEntity.asExternalModel() =
    ReadingRecord(
        id = id,
        isbn = isbn,
        date = date,
        cumulativePage = cumulativePage,
    )

fun ReadingRecord.asEntity() =
    ReadingRecordEntity(
        id = id,
        isbn = isbn,
        date = date,
        cumulativePage = cumulativePage,
    )
