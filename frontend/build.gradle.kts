plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
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
    js(IR) {
        moduleName = "stansted"
        browser {
            commonWebpackConfig {
                outputFileName = "stansted.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation("io.ktor:ktor-client-core:2.3.3")
                implementation("io.ktor:ktor-client-js:2.3.3")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.3")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.3")
            }
        }
    }
}

compose.experimental {
    web.application {}
}

configurations {
    create("output")
}

val bundle = task<Zip>("bundleDist") {
    dependsOn(tasks.findByPath(":build"))
    archiveBaseName.set("frontend")
    archiveExtension.set("jar")
    destinationDirectory.set(layout.buildDirectory.dir("jar"))
    from(layout.buildDirectory.dir("dist"))
    exclude("**/index.html")
}

artifacts {
    add("output", bundle.archiveFile) {
        builtBy(bundle)
        type = "jar"
    }
}
