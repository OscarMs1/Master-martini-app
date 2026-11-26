/*
 * Editor de reseñas de la app: TipTap (el mismo editor de la página web) dentro de un WebView.
 * Se empaqueta en app/src/main/assets/tiptap/editor.js con build.mjs (ver README de esta carpeta).
 *
 * Comunicación con Kotlin:
 *  - Kotlin -> JS: window.mmEditor.comando("bold" | "italic" | "bulletList" | "orderedList" | "undo" | "redo" | "clear")
 *  - JS -> Kotlin: window.MMAndroid.onChange(html, largoTexto, estadoJson)
 */
import { Editor } from "@tiptap/core";
import StarterKit from "@tiptap/starter-kit";

function avisar(editor) {
  const vacio = editor.isEmpty;
  const html = vacio ? "" : editor.getHTML();
  const estado = {
    bold: editor.isActive("bold"),
    italic: editor.isActive("italic"),
    bulletList: editor.isActive("bulletList"),
    orderedList: editor.isActive("orderedList"),
  };
  if (window.MMAndroid) window.MMAndroid.onChange(html, vacio ? 0 : editor.getText().replace(/\s+/g, " ").trim().length, JSON.stringify(estado));
}

const editor = new Editor({
  element: document.getElementById("editor"),
  extensions: [
    // Mismo formato que acepta el backend en reseñas: sin títulos, citas, código ni enlaces.
    StarterKit.configure({
      heading: false, blockquote: false, code: false, codeBlock: false, horizontalRule: false, link: false,
    }),
  ],
  editorProps: { attributes: { "aria-label": "Tu opinión del producto", class: "contenido" } },
  onUpdate: ({ editor: e }) => avisar(e),
  onSelectionUpdate: ({ editor: e }) => avisar(e),
});

window.mmEditor = {
  comando(nombre) {
    const c = editor.chain().focus();
    switch (nombre) {
      case "bold": c.toggleBold().run(); break;
      case "italic": c.toggleItalic().run(); break;
      case "bulletList": c.toggleBulletList().run(); break;
      case "orderedList": c.toggleOrderedList().run(); break;
      case "undo": c.undo().run(); break;
      case "redo": c.redo().run(); break;
      case "clear": editor.commands.clearContent(true); break;
    }
    avisar(editor);
  },
  // Al vaciar (después de publicar) se quita el foco para cerrar el teclado.
  ponerHtml(html) {
    editor.commands.setContent(html || "", { emitUpdate: true });
    if (html) editor.commands.focus("end"); else editor.commands.blur();
    avisar(editor);
  },
};

avisar(editor);
