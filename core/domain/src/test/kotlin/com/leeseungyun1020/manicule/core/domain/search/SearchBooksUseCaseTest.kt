package com.leeseungyun1020.manicule.core.domain.search

import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchBooksUseCaseTest {

    private val fakeBookRepository =
        object : BookRepository {
            override fun observeBook(isbn: String): Flow<Book?> = TODO("Not needed for test")

            override suspend fun syncBook(isbn: String): Result<Unit> = TODO("Not needed for test")

            override fun searchBooks(query: String): Flow<PagingData<Book>> {
                val dummyBook =
                    Book(
                        isbn = "1234567890",
                        title = "Test Book",
                        author = "Author",
                        publisher = "Publisher",
                        publishedDate = null,
                        coverUrl = "url",
                        totalPages = 100,
                        price = 10000,
                        category = "Category",
                        tableOfContentsUrl = null,
                        introductionUrl = null,
                        summaryUrl = null,
                    )
                return flowOf(PagingData.from(listOf(dummyBook)))
            }
        }

    private val useCase = SearchBooksUseCase(fakeBookRepository)

    @Test
    fun invoke_delegates_to_repository_searchBooks() =
        runTest {
            val resultFlow = useCase("query")

            resultFlow.collect { pagingData ->
                assertThat(pagingData).isNotNull()
            }
        }
}
