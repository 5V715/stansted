package dev.silas.domain

import java.time.OffsetDateTime
import java.util.UUID

data class Link(
    val id: UUID,
    val shortUrl: String,
    val fullUrl: String,
    val hits: Int,
    val createdAt: OffsetDateTime
)
