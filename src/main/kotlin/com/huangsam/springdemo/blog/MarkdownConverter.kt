package com.huangsam.springdemo.blog

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class MarkdownConverter {
    private val options = MutableDataSet()
    private val parser = Parser.builder(options).build()
    private val renderer = HtmlRenderer.builder(options).build()

    @Cacheable("markdown")
    fun convertToHtml(markdown: String): String {
        val document = parser.parse(markdown)
        return renderer.render(document)
    }
}
