package com.huangsam.springdemo.blog

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class MarkdownConverter {

    private val parser = Parser.builder().build()
    private val renderer = HtmlRenderer.builder().build()

    @Cacheable("markdown")
    fun convertToHtml(markdown: String?): String {
        if (markdown.isNullOrEmpty()) {
            return ""
        }
        val document = parser.parse(markdown)
        return renderer.render(document)
    }
}
