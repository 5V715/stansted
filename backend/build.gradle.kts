import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import io.zonky.test.db.postgres.embedded.FlywayPreparer
import org.flywaydb.core.api.Location
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.codegen.GenerationTool
import org.jooq.codegen.JavaGenerator
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Target

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin") version "2.3.5"
    id("org.jlleitschuh.gradle.ktlint")
}

buildscript {
    dependencies {
        classpath(platform("io.zonky.test.postgres:embedded-postgres-binaries-bom:15.2.0"))
        classpath("org.jooq:jooq-codegen:3.18.6")
        classpath("org.flywaydb:flyway-core:9.22.2")
        classpath("org.postgresql:postgresql:42.2.27")
        classpath("io.zonky.test:embedded-postgres:2.0.4")
    }
}

group = "dev.silas"

repositories {
    mavenCentral()
}

application {
    mainClass.set("dev.silas.App")
}

dependencies {

    implementation(project(":frontend")) {
        targetConfiguration = "output"
    }

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")
    implementation("io.ktor:ktor-server-auth-jvm:2.3.5")
    implementation("io.ktor:ktor-server-core-jvm:2.3.5")

    // ktor
    val ktor_version = "2.3.5"
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")

    // config
    implementation("com.sksamuel.hoplite:hoplite-core:2.7.5")

    // database
    implementation("org.postgresql:r2dbc-postgresql:1.0.2.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:1.0.1.RELEASE")
    implementation("org.jooq:jooq:3.18.7")
    implementation("org.flywaydb:flyway-core:9.22.2")
    implementation("org.postgresql:postgresql:42.6.0")

    // logging
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.zonky.test:embedded-postgres:2.0.4")
}

tasks {

    val generateJooqClasses by registering {
        finalizedBy(withType<KotlinCompile>())
        val outputDir = layout.buildDirectory.dir("jooq-generated")
        delete(outputDir)
        outputDir.get().asFile.mkdirs()
        outputs.dir(outputDir)
        doLast {
            EmbeddedPostgres
                .builder()
                .setLocaleConfig("locale", "en_US.UTF-8")
                .start().use { embedded ->
                    FlywayPreparer.fromConfiguration(
                        mapOf(
                            "flyway.locations" to "${Location.FILESYSTEM_PREFIX}${layout.projectDirectory.dir("src/main/resources/db/migration").asFile.absolutePath}"
                        )
                    )
                        .prepare(embedded.postgresDatabase)
                    GenerationTool
                        .generate(
                            Configuration()
                                .withGenerator(
                                    Generator()
                                        .withDatabase(
                                            Database()
                                                .withIncludeSystemSequences(true)
                                                .withIncludes("public.*|pg_catalog.pg_advisory_xact_lock|pg_catalog.pg_try_advisory_lock|pg_catalog.pg_advisory_unlock")
                                        )
                                        .withName(JavaGenerator::class.qualifiedName)
                                        .withTarget(
                                            Target()
                                                .withEncoding("UTF-8")
                                                .withPackageName("dev.silas.stansted.db.model")
                                                .withDirectory(outputDir.get().asFile.absolutePath)
                                        )
                                )
                                .withJdbc(
                                    Jdbc()
                                        .withUrl(embedded.getJdbcUrl("postgres", "postgres"))
                                        .withDriver("org.postgresql.Driver")
                                )
                        )
                }
        }
    }

    test {
        useJUnitPlatform {
            excludeTags("manual")
        }
    }

    withType<KotlinCompile> {
        dependsOn(generateJooqClasses)
        with(compilerOptions) {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(listOf("-Xjsr305=strict", "-Xcontext-receivers"))
        }
    }
}

jib {
    from {
        image = "arm64v8/eclipse-temurin"
        platforms {
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    to {
        image = "ghcr.io/5v715/stansted"
        tags = when (val versionString = version.toString()) {
            "unspecified" -> setOf("latest")
            else -> setOf("latest", versionString)
        }
    }
}

with(extensions) {
    findByType<JavaPluginExtension>()?.run {
        sourceSets.findByName(MAIN_SOURCE_SET_NAME)?.also { mainSourceSet ->
            mainSourceSet.java(
                object : Action<SourceDirectorySet> {
                    override fun execute(t: SourceDirectorySet) {
                        when (val task = tasks.findByName("generateJooqClasses")) {
                            is Task -> {
                                t.srcDirs(task.outputs)
                            }
                        }
                    }
                }
            )
        }
    }
}
