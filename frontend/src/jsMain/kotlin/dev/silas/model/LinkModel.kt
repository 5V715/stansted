@file:OptIn(ExperimentalSerializationApi::class)

package dev.silas.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class Link(
    val id: String,
    @JsonNames("short_url")
    val shortUrl: String,
    @JsonNames("full_url")
    val fullUrl: String,
    @JsonNames("created_at")
    val createdAt: String,
    val hits: List<Hit> = emptyList()
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

@Serializable
data class Update(
    val old: Link? = null,
    val new: Link? = null,
    val type: Type
) {
    @Serializable
    enum class Type {
        INSERT, UPDATE, DELETE, TRUNCATE
    }
}
