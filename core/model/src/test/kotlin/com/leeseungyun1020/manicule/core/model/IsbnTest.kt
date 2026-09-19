package com.leeseungyun1020.manicule.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IsbnTest {

    @Test
    fun `parseOrNull returns normalized Isbn for valid ISBN-13`() {
        val isbn = Isbn.parseOrNull("978-89-546-9991-4")
        assertThat(isbn).isNotNull()
        assertThat(isbn?.value).isEqualTo("9788954699914")
    }

    @Test
    fun `parseOrNull returns normalized Isbn for valid ISBN-10 with X check digit`() {
        val isbn = Isbn.parseOrNull("0-8044-2957-x")
        assertThat(isbn).isNotNull()
        assertThat(isbn?.value).isEqualTo("080442957X")
    }

    @Test
    fun `parseOrNull strips hyphens and spaces`() {
        val isbn = Isbn.parseOrNull("  979 1 161 75969 2  ")
        assertThat(isbn).isNotNull()
        assertThat(isbn?.value).isEqualTo("9791161759692")
    }

    @Test
    fun `ISBN typealias works equivalently`() {
        val isbn = ISBN.parseOrNull("978-89-546-9991-4")
        assertThat(isbn).isNotNull()
        assertThat(isbn?.value).isEqualTo("9788954699914")
    }

    @Test
    fun `parseOrNull returns null for invalid ISBN-13 checksum`() {
        // Last digit changed from 4 to 5
        val isbn = Isbn.parseOrNull("9788954699915")
        assertThat(isbn).isNull()
    }

    @Test
    fun `parseOrNull returns null for invalid ISBN-10 checksum`() {
        // Last digit changed from X to 0
        val isbn = Isbn.parseOrNull("0804429570")
        assertThat(isbn).isNull()
    }

    @Test
    fun `parseOrNull returns null for non-book 13-digit EAN`() {
        // EAN starts with 880 (not 978 or 979)
        val isbn = Isbn.parseOrNull("8801234567890")
        assertThat(isbn).isNull()
    }

    @Test
    fun `parseOrNull returns null for plain text and book titles`() {
        assertThat(Isbn.parseOrNull("1984")).isNull()
        assertThat(Isbn.parseOrNull("Kotlin in Action")).isNull()
        assertThat(Isbn.parseOrNull("")).isNull()
        assertThat(Isbn.parseOrNull("1234567890")).isNull()
    }
}
