package com.huangsam.springdemo.blog

import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

fun LocalDateTime.format(): String = this.format(englishDateFormatter)

fun String.toSlug() = lowercase(Locale.getDefault())
    .replace("\n", " ")
    .replace("[^a-z\\d\\s]".toRegex(), " ")
    .split(" ")
    .joinToString("-")
    .replace("-+".toRegex(), "-")

private object Day {
    const val ONE = 1
    const val TWO = 2
    const val THREE = 3
    const val TEN = 10
    const val ELEVEN = 11
    const val THIRTEEN = 13
    const val MIN = ONE
    const val MAX = 31
}

private val daysLookup = (Day.MIN..Day.MAX).associate { it.toLong() to getOrdinal(it) }

private val englishDateFormatter = DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd")
    .appendLiteral(" ")
    .appendText(ChronoField.DAY_OF_MONTH, daysLookup)
    .appendLiteral(" ")
    .appendPattern("yyyy")
    .toFormatter(Locale.ENGLISH)

private fun getOrdinal(n: Int) = when {
    n in Day.ELEVEN..Day.THIRTEEN -> "${n}th"
    n % Day.TEN == Day.ONE -> "${n}st"
    n % Day.TEN == Day.TWO -> "${n}nd"
    n % Day.TEN == Day.THREE -> "${n}rd"
    else -> "${n}th"
}
