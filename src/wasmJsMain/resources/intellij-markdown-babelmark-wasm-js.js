import * as foo from "./intellij-markdown-babelmark-wasm-js.mjs"

let keys = Object.keys(foo);

export default keys.length === 1 && keys[0] === "default" ? { ... foo.default } : foo;