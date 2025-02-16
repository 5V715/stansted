import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "stansted"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "stansted.js"
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static =
                            (static ?: mutableListOf()).apply {
                                // Serve sources to debug inside browser
                                add(rootDirPath)
                                add(projectDirPath)
                            }
                    }
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.bundles.ktor.client)
        }
    }
}

configurations {
    create("output")
}

val bundle = task<Zip>("bundleDist") {
    val task = tasks.findByPath(":frontend:wasmJsBrowserProductionWebpack")
    val resources = tasks.findByPath(":frontend:wasmJsProcessResources")
    dependsOn(task)
    archiveBaseName.set("frontend")
    archiveExtension.set("jar")
    destinationDirectory.set(layout.buildDirectory.dir("jar"))
    from(task?.outputs)
    from(resources?.outputs)
}

artifacts {
    add("output", bundle.archiveFile) {
        builtBy(bundle)
        type = "jar"
    }
}