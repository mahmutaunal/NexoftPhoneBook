package com.mahmutalperenunal.nexoftphonebook.domain.entity

data class SearchHistoryItem(
    val id: Long,
    val query: String,
    val createdAt: Long
)