package com.leeseungyun1020.manicule.core.model

@JvmInline
value class Isbn private constructor(
    val value: String,
) {

    companion object {
        private val ISBN_10_REGEX = Regex("^[0-9]{9}[0-9X]$")
        private val ISBN_13_REGEX = Regex("^(978|979)[0-9]{10}$")

        fun parseOrNull(raw: String): Isbn? {
            val normalized = normalize(raw)
            return if (isValidNormalized(normalized)) Isbn(normalized) else null
        }

        fun normalize(raw: String): String = raw.filter { it != '-' && !it.isWhitespace() }.uppercase()

        private fun isValidNormalized(value: String): Boolean =
            when {
                ISBN_10_REGEX.matches(value) -> verifyIsbn10Checksum(value)
                ISBN_13_REGEX.matches(value) -> verifyIsbn13Checksum(value)
                else -> false
            }

        private fun verifyIsbn10Checksum(isbn10: String): Boolean {
            var sum = 0
            for (i in 0 until 9) {
                sum += (isbn10[i] - '0') * (10 - i)
            }
            val checkChar = isbn10[9]
            sum += if (checkChar == 'X') 10 else (checkChar - '0')
            return sum % 11 == 0
        }

        private fun verifyIsbn13Checksum(isbn13: String): Boolean {
            var sum = 0
            for (i in 0 until 12) {
                val digit = isbn13[i] - '0'
                sum += if (i % 2 == 0) digit else digit * 3
            }
            val checkDigit = (10 - (sum % 10)) % 10
            return (isbn13[12] - '0') == checkDigit
        }
    }
}

typealias ISBN = Isbn
