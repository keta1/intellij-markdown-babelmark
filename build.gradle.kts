import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.ir.Executable

plugins {
    kotlin("multiplatform") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-browser-wasm-js:0.3")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    commonMainImplementation("org.jetbrains:markdown:0.7.3")
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
}
