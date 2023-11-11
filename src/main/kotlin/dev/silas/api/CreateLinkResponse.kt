package dev.silas.api

import dev.silas.domain.Link
import kotlinx.serialization.Serializable

@Serializable
data class CreateLinkResponse(
    val id: String,
    val shortUrl: String,
    val fullUrl: String,
    val hits: Int,
    val createdAt: String
)

fun Link.toResponse() = CreateLinkResponse(
    id = id.toString(),
    shortUrl = shortUrl,
    fullUrl = fullUrl,
    hits = hits.toInt(),
    createdAt = createdAt.toString()
)
