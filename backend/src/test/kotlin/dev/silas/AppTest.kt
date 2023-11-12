package dev.silas

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import kotlin.test.Test

class AppTest {

    companion object {

        private lateinit var postgres: EmbeddedPostgres

        @BeforeAll
        @JvmStatic
        fun `setup embedded postgres`() {
            postgres = EmbeddedPostgres.builder().start()
            System.setProperty("config.override.postgres.port", postgres.port.toString())
            // System.setProperty("config.override.auth.password", "test")
            println(postgres.getJdbcUrl("postgres", "postgres"))
        }

        @AfterAll
        @JvmStatic
        fun `stop embedded postgres`() {
            postgres.close()
        }
    }

    @Test
    fun `can start`() {
        App.main(args = arrayOf())
    }
}
