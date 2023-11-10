package dev.silas.domain

import java.math.BigInteger
import java.util.*

data class Link(
    val id: UUID,
    val shortUrl: String,
    val fullUrl: String,
    val hits: BigInteger
)
