package dev.silas.model

import kotlinx.serialization.Serializable

@Serializable
data class Link(
    val id: String,
    val shortUrl: String,
    val fullUrl: String,
    val createdAt: String,
    val hits: Int
)

@Serializable
data class NewLink(
    val shortUrl: String? = null,
    val fullUrl: String
)
