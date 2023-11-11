package dev.silas.api

import kotlinx.serialization.Serializable

@Serializable
data class CreateLinkRequest(
    val shortUrl: String? = null,
    val fullUrl: String
)
