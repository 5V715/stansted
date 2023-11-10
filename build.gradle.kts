import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.5"
    kotlin("plugin.serialization") version "1.9.10"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}

group = "dev.silas"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")

    // ktor
    implementation("io.ktor:ktor-server-core:2.3.5")
    implementation("io.ktor:ktor-server-netty:2.3.5")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")

    // config
    implementation("com.sksamuel.hoplite:hoplite-core:2.7.5")

    // database
    implementation("org.postgresql:r2dbc-postgresql:1.0.2.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:1.0.1.RELEASE")
    implementation("org.flywaydb:flyway-core:9.22.2")
    implementation("org.postgresql:postgresql:42.6.0")

    testImplementation("org.slf4j:slf4j-api:2.0.9")
    testImplementation("org.slf4j:slf4j-simple:2.0.9")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.zonky.test:embedded-postgres:2.0.4")
}

tasks {

    test {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
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
        image = "ghcr.io/5v715/stansted:latest"
    }
}
