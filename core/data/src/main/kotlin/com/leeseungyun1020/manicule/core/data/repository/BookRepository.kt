package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.model.Book

interface BookRepository {
    suspend fun getBook(isbn: String): Book?

    suspend fun fetchAndCacheBook(isbn: String): Book?
}
