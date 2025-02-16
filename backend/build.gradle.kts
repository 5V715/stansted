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
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ktlint)
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.7.3")

    // ktor
    implementation(libs.bundles.ktor.server)

    // config
    implementation("com.sksamuel.hoplite:hoplite-core:2.7.5")

    // database
    implementation(libs.bundles.database)

    // logging
    implementation("ch.qos.logback:logback-classic:1.4.12")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.10")
    testImplementation("io.zonky.test:embedded-postgres:2.0.4")
}

tasks {

    val generateJooqClasses by registering {
        val outputDir = layout.buildDirectory.dir("jooq-generated")
        outputs.dir(outputDir)
        doFirst {
            delete(outputDir)
            outputDir.get().asFile.mkdirs()
        }
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
            jvmTarget.set(JvmTarget.JVM_21)
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
