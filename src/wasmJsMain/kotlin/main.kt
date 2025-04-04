import icu.ketal.markdown.babelmark.BuildKonfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.space.SFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.w3c.dom.url.URL
import org.w3c.fetch.Headers
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit

@OptIn(ExperimentalJsExport::class)
@JsExport
fun fetch(request: Request): Response {
    if (request.method != "GET") {
        return Response(
            "Method not allowed".toJsString(),
            ResponseInit(status = 405)
        )
    }
    val headers = Headers().apply {
        append("content-type", "application/json")
    }
    val url = URL(request.url)
    val text = url.searchParams.get("text").orEmpty()
    val mode = url.searchParams.get("mode").orEmpty()
    if (text.length > 1000) {
        return Response(
            "Input text is too long".toJsString(),
            ResponseInit(status = 400)
        )
    }
    val flavour = when (mode) {
        "commonmark" -> CommonMarkFlavourDescriptor()
        "gfm" -> GFMFlavourDescriptor()
        "sfm" -> SFMFlavourDescriptor(true)
        else -> CommonMarkFlavourDescriptor()
    }
    MarkdownParser(flavour).buildMarkdownTreeFromString(text = text)

    val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(text)
    val html = HtmlGenerator(text, parsedTree, flavour).generateHtml()
    val result = MarkdownResult(
        html = html,
        name = "intellij-markdown-${mode.ifBlank { "commonmark" }}",
        version = BuildKonfig.markdown_version
    )
    return Response(
        Json.encodeToString(result).toJsString(),
        ResponseInit(headers = headers)
    )
}

@Serializable
data class MarkdownResult(
    val html: String,
    val name: String,
    val version: String,
)
