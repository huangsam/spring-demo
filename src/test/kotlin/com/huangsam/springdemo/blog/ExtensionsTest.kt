package com.huangsam.springdemo.blog

import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExtensionsTest {
    @Test
    fun `toSlug converts simple string to lowercase hyphenated`() {
        assertEquals("hello-world", "Hello World".toSlug())
    }

    @Test
    fun `toSlug removes special characters`() {
        assertEquals("hello-world", "Hello, World".toSlug())
    }

    @Test
    fun `toSlug replaces newlines with hyphens`() {
        assertEquals("hello-world", "Hello\nWorld".toSlug())
    }

    @Test
    fun `toSlug collapses multiple spaces`() {
        assertEquals("hello-world", "Hello   World".toSlug())
    }

    @Test
    fun `toSlug handles already lowercase string`() {
        assertEquals("spring-demo", "spring demo".toSlug())
    }

    @Test
    fun `format returns date with ordinal st`() {
        val dt = LocalDateTime.of(2024, 1, 1, 0, 0)
        assertEquals("January 1st 2024, Monday", dt.format())
    }

    @Test
    fun `format returns date with ordinal nd`() {
        val dt = LocalDateTime.of(2024, 6, 2, 0, 0)
        assertEquals("June 2nd 2024, Sunday", dt.format())
    }

    @Test
    fun `format returns date with ordinal rd`() {
        val dt = LocalDateTime.of(2024, 6, 3, 0, 0)
        assertEquals("June 3rd 2024, Monday", dt.format())
    }

    @Test
    fun `format returns date with ordinal th for 11th`() {
        val dt = LocalDateTime.of(2024, 6, 11, 0, 0)
        assertEquals("June 11th 2024, Tuesday", dt.format())
    }

    @Test
    fun `format returns date with ordinal th for 12th`() {
        val dt = LocalDateTime.of(2024, 6, 12, 0, 0)
        assertEquals("June 12th 2024, Wednesday", dt.format())
    }

    @Test
    fun `format returns date with ordinal th for 13th`() {
        val dt = LocalDateTime.of(2024, 6, 13, 0, 0)
        assertEquals("June 13th 2024, Thursday", dt.format())
    }

    @Test
    fun `format returns date with ordinal th for generic day`() {
        val dt = LocalDateTime.of(2024, 6, 15, 0, 0)
        assertEquals("June 15th 2024, Saturday", dt.format())
    }
}
