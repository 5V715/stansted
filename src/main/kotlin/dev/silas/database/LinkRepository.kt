package dev.silas.database

import dev.silas.DatabaseConnectionPool
import dev.silas.domain.Link
import io.r2dbc.spi.Row
import java.math.BigInteger
import java.time.OffsetDateTime
import java.util.UUID

class LinkRepository : DatabaseAccess() {

    data class NewLink(
        val shortUrl: String,
        val fullUrl: String
    )

    context(DatabaseConnectionPool)
    suspend fun insert(link: NewLink): Link? {
        val result =
            runQuery("insert into links (short_url,full_url) values ('${link.shortUrl}', '${link.fullUrl}') returning *") { row, _ ->
                row.toLink()
            }
        return when (result) {
            is List<Link> -> result.firstOrNull()
            else -> null
        }
    }

    context(DatabaseConnectionPool)
    suspend fun getAll(): List<Link>? =
        runQuery("select * from links order by created_at desc") { row, _ ->
            row.toLink()
        }

    context(DatabaseConnectionPool)
    suspend fun findByShortUrl(shortUrl: String): Link? {
        val result = runQuery("update links set hits = hits + 1 where short_url = '$shortUrl' returning *") { row, _ ->
            row.toLink()
        }
        return when (result) {
            is List<Link> -> result.firstOrNull()
            else -> null
        }
    }

    private fun Row.toLink() =
        Link(
            id = this["id", UUID::class.java]!!,
            shortUrl = this["short_url", String::class.java]!!,
            fullUrl = this["full_url", String::class.java]!!,
            hits = this["hits", BigInteger::class.java]!!,
            createdAt = this["created_at", OffsetDateTime::class.java]!!
        )
}
