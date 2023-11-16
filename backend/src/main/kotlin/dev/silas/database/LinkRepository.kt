package dev.silas.database

import dev.silas.JooqDslAccess
import dev.silas.domain.Link
import dev.silas.stansted.db.model.public_.Tables
import dev.silas.stansted.db.model.public_.tables.records.LinksRecord
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class LinkRepository {

    data class NewLink(
        val shortUrl: String,
        val fullUrl: String
    )

    context(JooqDslAccess)
    suspend fun insert(link: NewLink): Link? {
        val result = with(Tables.LINKS) {
            Flux.from(
                dslContext.insertInto(this)
                    .columns(SHORT_URL, FULL_URL)
                    .values(link.shortUrl, link.fullUrl)
                    .returning()
            )
        }.map(::toLink)
            .collectList()
            .awaitSingle()
        return when (result) {
            is List<Link> -> result.firstOrNull()
            else -> null
        }
    }

    context(JooqDslAccess)
    suspend fun getAll(): List<Link> = with(Tables.LINKS) {
        Flux.from(dslContext.selectFrom(this))
            .map(::toLink)
            .collectList()
            .awaitSingle()
    }

    context(JooqDslAccess)
    suspend fun findAndHit(shortUrl: String): Link? =
        with(Tables.LINKS) {
            Mono.from(
                dslContext
                    .update(this)
                    .set(HITS, HITS.plus(1))
                    .where(SHORT_URL.eq(shortUrl))
                    .returning()
            )
                .map(::toLink)
                .awaitSingleOrNull()
        }

    private fun toLink(linksRecord: LinksRecord) = Link(
        id = linksRecord.id,
        shortUrl = linksRecord.shortUrl,
        fullUrl = linksRecord.fullUrl,
        hits = linksRecord.hits,
        createdAt = linksRecord.createdAt
    )
}
