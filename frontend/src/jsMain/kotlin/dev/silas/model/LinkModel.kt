package dev.silas.model

import kotlinx.serialization.Serializable

@Serializable
data class Link(
    val id: String,
    val shortUrl: String,
    val fullUrl: String,
    val createdAt: String,
    val hits: List<Hit>
)

@Serializable
data class Hit(
    val createdAt: String,
    val data: Map<String, List<String>>
)

@Serializable
data class NewLink(
    val shortUrl: String? = null,
    val fullUrl: String
)
