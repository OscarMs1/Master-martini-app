// Empaqueta el editor TipTap de reseñas para la app (un solo archivo, sin internet).
// Usa los paquetes @tiptap ya instalados en la página web (Master-Martini/frontend).
//
//   node tiptap-editor/build.mjs [ruta/a/frontend]
//
// Por defecto busca la web en ../scroll/frontend. Genera app/src/main/assets/tiptap/editor.js.
import { createRequire } from "node:module";
import path from "node:path";
import { fileURLToPath } from "node:url";

const aqui = path.dirname(fileURLToPath(import.meta.url));
const frontend = path.resolve(process.argv[2] ?? path.join(aqui, "..", "..", "scroll", "frontend"));
const require = createRequire(path.join(frontend, "package.json"));
const esbuild = require("esbuild");

await esbuild.build({
  entryPoints: [path.join(aqui, "editor.src.js")],
  outfile: path.join(aqui, "..", "app", "src", "main", "assets", "tiptap", "editor.js"),
  bundle: true,
  minify: true,
  format: "iife",
  target: ["chrome90"],
  nodePaths: [path.join(frontend, "node_modules")],
  legalComments: "eof",
});
console.log("Listo: app/src/main/assets/tiptap/editor.js");
