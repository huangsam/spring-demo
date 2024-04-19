package com.huangsam.springdemo.blog

import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

object Day {
    const val MINIMUM = 1
    const val MAXIMUM = 31
}

object Token {
    const val ONE = 1
    const val TWO = 2
    const val THREE = 3
    const val TEN = 10
    const val ELEVEN = 11
    const val THIRTEEN = 13
}

fun LocalDateTime.format(): String = this.format(englishDateFormatter)

private val daysLookup = (Day.MINIMUM..Day.MAXIMUM).associate { it.toLong() to getOrdinal(it) }

private val englishDateFormatter = DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd")
    .appendLiteral(" ")
    .appendText(ChronoField.DAY_OF_MONTH, daysLookup)
    .appendLiteral(" ")
    .appendPattern("yyyy")
    .toFormatter(Locale.ENGLISH)

private fun getOrdinal(n: Int) = when {
    n in Token.ELEVEN..Token.THIRTEEN -> "${n}th"
    n % Token.TEN == Token.ONE -> "${n}st"
    n % Token.TEN == Token.TWO -> "${n}nd"
    n % Token.TEN == Token.THREE -> "${n}rd"
    else -> "${n}th"
}

fun String.toSlug() = lowercase(Locale.getDefault())
    .replace("\n", " ")
    .replace("[^a-z\\d\\s]".toRegex(), " ")
    .split(" ")
    .joinToString("-")
    .replace("-+".toRegex(), "-")
