package com.huangsam.springdemo.blog

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class MarkdownConverterTest
@Autowired
constructor(private val markdownConverter: MarkdownConverter) {
    @Test
    internal fun `convertToHtml renders bold text`() {
        val html = markdownConverter.convertToHtml("**bold**")
        assertTrue(html.contains("<strong>bold</strong>"))
    }

    @Test
    internal fun `convertToHtml renders italic text`() {
        val html = markdownConverter.convertToHtml("*italic*")
        assertTrue(html.contains("<em>italic</em>"))
    }

    @Test
    internal fun `convertToHtml renders unordered list`() {
        val html = markdownConverter.convertToHtml("- item1\n- item2")
        assertTrue(html.contains("<ul>"))
        assertTrue(html.contains("<li>item1</li>"))
        assertTrue(html.contains("<li>item2</li>"))
    }

    @Test
    internal fun `convertToHtml renders link`() {
        val html = markdownConverter.convertToHtml("[click](https://example.com)")
        assertTrue(html.contains("<a href=\"https://example.com\">click</a>"))
    }

    @Test
    internal fun `convertToHtml renders heading`() {
        val html = markdownConverter.convertToHtml("# Heading")
        assertTrue(html.contains("<h1>Heading</h1>"))
    }

    @Test
    internal fun `convertToHtml handles empty string`() {
        val html = markdownConverter.convertToHtml("")
        assertTrue(html.isEmpty())
    }
}
