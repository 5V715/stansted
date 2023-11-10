package dev.silas.database

import dev.silas.DatabaseAccess
import dev.silas.DatabaseConnectionPool
import dev.silas.domain.Link
import java.math.BigInteger
import java.util.*

class LinkRepository : DatabaseAccess() {

    data class NewLink(
        val shortUrl: String,
        val fullUrl: String
    )

    context(DatabaseConnectionPool)
    suspend fun insert(link: NewLink): List<UUID>? =
        runQuery("insert into links (short_url,full_url) values ('${link.shortUrl}', '${link.fullUrl}') returning id") { row, _ ->
            row["id", UUID::class.java]!!
        }

    context(DatabaseConnectionPool)
    suspend fun getAll(): List<Link>? =
        runQuery("select * from links") { row, _ ->
            Link(
                id = row["id", UUID::class.java]!!,
                shortUrl = row["short_url", String::class.java]!!,
                fullFull = row["full_url", String::class.java]!!,
                hits = row["hits", BigInteger::class.java]!!
            )
        }

    context(DatabaseConnectionPool)
    suspend fun findByShortUrl(shortUrl: String): Link? {
        val result = runQuery("update links set hits = hits + 1 where short_url = '$shortUrl' returning *") { row, _ ->
            Link(
                id = row["id", UUID::class.java]!!,
                shortUrl = row["short_url", String::class.java]!!,
                fullFull = row["full_url", String::class.java]!!,
                hits = row["hits", BigInteger::class.java]!!
            )
        }
        return when (result) {
            is List<Link> -> result.firstOrNull()
            else -> null
        }
    }
}
