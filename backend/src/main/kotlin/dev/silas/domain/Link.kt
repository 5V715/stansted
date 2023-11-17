package dev.silas.domain

import java.time.OffsetDateTime
import java.util.UUID

data class Link(
    val id: UUID,
    val shortUrl: String,
    val fullUrl: String,
    val createdAt: OffsetDateTime,
    val hits: List<Hit>
)

data class Hit(
    val data: Map<String, List<String>>,
    val createdAt: OffsetDateTime
)
