import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
}

kotlin {

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {

        jsMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation("io.ktor:ktor-client-core:2.3.3")
            implementation("io.ktor:ktor-client-js:2.3.3")
            implementation("io.ktor:ktor-client-websockets:2.3.3")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.3")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.3")
        }
    }
}

configurations {
    create("output")
}

val bundle = tasks.register<Zip>("bundleDist") {
    dependsOn(":frontend:jsBrowserDistribution")
    archiveBaseName.set("frontend")
    archiveExtension.set("jar")
    destinationDirectory.set(layout.buildDirectory.dir("jar"))
    from(layout.buildDirectory.dir("dist/js/productionExecutable")) {
        into("static")
    }
}

artifacts {
    add("output", bundle.get().archiveFile) {
        builtBy(bundle)
        type = "jar"
    }
}
