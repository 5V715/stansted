package dev.silas.database

import dev.silas.DatabaseConnectionPool
import io.r2dbc.spi.Connection
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.withContext
import reactor.core.publisher.Mono

abstract class DatabaseAccess {
    context(DatabaseConnectionPool)
    suspend fun <T> runQuery(query: String, mapping: (Row, RowMetadata) -> T): List<T>? =
        withContext(Dispatchers.IO) {
            Mono.usingWhen(
                connectionPool.create(),
                { connection ->
                    Mono.from(
                        connection
                            .createStatement(query)
                            .execute()
                    )
                },
                Connection::close
            ).flatMapMany {
                it.map(mapping)
            }
                .collectList()
                .awaitFirstOrNull()
        }
}
