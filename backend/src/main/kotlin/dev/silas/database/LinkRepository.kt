package dev.silas.database

import dev.silas.JooqDslAccess
import dev.silas.JsonSerializationAccess
import dev.silas.domain.Hit
import dev.silas.domain.Link
import dev.silas.stansted.db.model.public_.Tables.HITS
import dev.silas.stansted.db.model.public_.Tables.LINKS
import dev.silas.stansted.db.model.public_.tables.records.LinksRecord
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.jooq.Condition
import org.jooq.JSONB
import org.jooq.Records
import org.jooq.impl.DSL
import reactor.core.publisher.Flux

class LinkRepository {

    data class NewLink(
        val shortUrl: String,
        val fullUrl: String
    )

    context(JooqDslAccess)
    suspend fun insert(link: NewLink): Link? {
        val result = with(LINKS) {
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

    context(JooqDslAccess, JsonSerializationAccess)
    suspend fun getAll(): List<Link> = fetchLinks()
        .collectList()
        .awaitSingle()

    context(JooqDslAccess, JsonSerializationAccess)
    suspend fun findAndHit(shortUrl: String, data: Map<String, List<String>>): Link? =
        Flux.from(dslContext.selectFrom(LINKS).where(LINKS.SHORT_URL.eq(shortUrl)))
            .flatMap { link ->
                dslContext
                    .insertInto(HITS)
                    .columns(HITS.LINK_ID, HITS.DATA)
                    .values(link.id, JSONB.valueOf(json.encodeToString<Map<String, List<String>>>(data)))
            }.thenMany(
                fetchLinks(LINKS.SHORT_URL.eq(shortUrl))
                    .collectList()
                    .mapNotNull { it.firstOrNull() }
            )
            .collectList()
            .mapNotNull { it.firstOrNull() }
            .awaitSingleOrNull()

    private fun toLink(linksRecord: LinksRecord, hits: List<Hit> = emptyList()) = Link(
        id = linksRecord.id,
        shortUrl = linksRecord.shortUrl,
        fullUrl = linksRecord.fullUrl,
        hits = hits,
        createdAt = linksRecord.createdAt
    )

    context(JsonSerializationAccess, JooqDslAccess)
    private fun fetchLinks(condition: Condition? = null): Flux<Link> = Flux.from(
        dslContext
            .select(
                LINKS.ID,
                LINKS.SHORT_URL,
                LINKS.FULL_URL,
                LINKS.CREATED_AT,
                DSL.multiset(
                    DSL.select(HITS.CREATED_AT, HITS.DATA)
                        .from(HITS)
                        .where(HITS.links().ID.eq(LINKS.ID))
                ).convertFrom {
                    it.map(
                        Records.mapping { createAt, data ->
                            Hit(json.decodeFromString(data.data()), createAt)
                        }
                    )
                }
            )
            .from(LINKS)
            .also {
                if (condition != null) {
                    it.where(condition)
                }
            }
    ).map { (id, shortUrl, fullUrl, createdAt, hits) ->
        Link(
            id = id,
            shortUrl = shortUrl,
            fullUrl = fullUrl,
            createdAt = createdAt,
            hits = hits
        )
    }
}
