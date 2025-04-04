# Babelmark Dingus server for intellij markdown

[Babelmark](https://babelmark.github.io/) is a tool to compare the output of markdown implementations.

This repository is the source of <https://babelmark.ketal.icu>, which provides Babelmark with various Kotlin-based markdown parsers/renderers.

## Wrangler

Configure the [wrangler.toml](wrangler.toml) by filling in the `account_id` from the Workers pages of your Cloudflare Dashboard.

Further documentation for Wrangler can be found [here](https://developers.cloudflare.com/workers/tooling/wrangler).

## Build & Deploy

After setting up your environment, run the following command:

```shell
./gradlew :compileProductionExecutableKotlinWasmJs
```

That will compile your code into a WebAssembly executable and JavaScript glue code, 
after which you can run `wrangler deploy` to push it to Cloudflare:

```shell
npx wrangler@latest deploy build/js/packages/intellij-markdown-babelmark-wasm-js/kotlin/intellij-markdown-babelmark-wasm-js.js
```

## Learn more

* [Kotlin/Wasm Overview](https://kotl.in/wasm/)
* [Kotlin/Wasm JavaScript interop](https://kotlinlang.org/docs/wasm-js-interop.html).
