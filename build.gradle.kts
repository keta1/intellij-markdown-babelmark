import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.ir.Executable

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    id("com.codingfeline.buildkonfig") version "0.17.0"
}

repositories {
    mavenCentral()
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        nodejs()

        // Workaround for https://youtrack.jetbrains.com/issue/KT-66972
        compilations
            .configureEach {
                binaries.withType<Executable>().configureEach {
                        linkTask.configure {
                            val moduleName = linkTask.flatMap {
                                it.compilerOptions.moduleName
                            }
                            val fileName = moduleName.map { module ->
                                "$module.uninstantiated.mjs"
                            }

                            val mjsFile: Provider<RegularFile> = linkTask.flatMap {
                                it.destinationDirectory.file(fileName.get())
                            }

                            doLast {
                                val module = moduleName.get()
                                val file = mjsFile.get().asFile
                                val text = file.readText()
                                val newText = text
                                    .replace("if \\(!\\w+( && !\\w+)*\\) \\{[^\\}]*\\}".toRegex(), "")
                                    .replace(
                                        "(if \\(isBrowser\\) \\{\\s*wasmInstance[^\\}]*\\})".toRegex(),
                                        """
                                              const isWorker = navigator.userAgent.includes("Workers")
                                              if (isWorker) {
                                                  const { default: wasmModule } = await import('./$module.wasm');
                                                  wasmInstance = (await WebAssembly.instantiate(wasmModule, importObject));
                                              }
                                        """.trimIndent()
                                    )

                                file.writeText(newText)
                            }
                        }
                    }
            }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.browser)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.markdown)
        }
    }
}

buildkonfig {
    packageName = "icu.ketal.markdown.babelmark"

    defaultConfigs {
        buildConfigField(STRING, "markdown_version", libs.versions.markdown.get())
    }
}
