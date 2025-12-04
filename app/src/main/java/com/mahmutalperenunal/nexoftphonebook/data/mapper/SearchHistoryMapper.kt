package com.mahmutalperenunal.nexoftphonebook.data.mapper

import com.mahmutalperenunal.nexoftphonebook.data.local.entity.SearchHistoryEntity
import com.mahmutalperenunal.nexoftphonebook.domain.entity.SearchHistoryItem

fun SearchHistoryEntity.toDomain(): SearchHistoryItem =
    SearchHistoryItem(
        id = id,
        query = query,
        createdAt = createdAt
    )