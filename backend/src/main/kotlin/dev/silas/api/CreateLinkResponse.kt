package dev.silas.api

import dev.silas.domain.Link
import kotlinx.serialization.Serializable

@Serializable
data class CreateLinkResponse(
    val id: String,
    val shortUrl: String,
    val fullUrl: String,
    val hits: List<HitResponse>,
    val createdAt: String
)

@Serializable
data class HitResponse(
    val createdAt: String,
    val data: Map<String, List<String>>
)

fun Link.toResponse() = CreateLinkResponse(
    id = id.toString(),
    shortUrl = shortUrl,
    fullUrl = fullUrl,
    hits = hits.map { HitResponse(it.createdAt.toString(), it.data) },
    createdAt = createdAt.toString()
)
