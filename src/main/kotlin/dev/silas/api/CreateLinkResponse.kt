package dev.silas.api

import kotlinx.serialization.Serializable

@Serializable
data class CreateLinkResponse(
    val id: String
)
