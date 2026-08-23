package com.leeseungyun1020.manicule.core.domain.book

import androidx.paging.PagingData
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetBookDetailUseCaseTest {
    private val repository = FakeBookRepository()
    private val useCase = GetBookDetailUseCase(repository)

    @Test
    fun invoke_observesCachedBook() =
        runTest {
            useCase("123").test {
                assertThat(awaitItem()).isNull()
                repository.books.value = testBook
                assertThat(awaitItem()).isEqualTo(testBook)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun refresh_delegatesToRepository() =
        runTest {
            repository.refreshResult = Result.failure(IllegalStateException("failed"))

            val result = useCase.refresh("123")

            assertThat(result.isFailure).isTrue()
            assertThat(repository.refreshedIsbn).isEqualTo("123")
        }

    private class FakeBookRepository : BookRepository {
        val books = MutableStateFlow<Book?>(null)
        var refreshResult: Result<Unit> = Result.success(Unit)
        var refreshedIsbn: String? = null

        override fun observeBook(isbn: String): Flow<Book?> = books

        override suspend fun syncBook(isbn: String): Result<Unit> {
            refreshedIsbn = isbn
            return refreshResult
        }

        override fun searchBooks(query: String): Flow<PagingData<Book>> = emptyFlow()
    }

    private companion object {
        val testBook =
            Book(
                isbn = "123",
                title = "Book",
                author = "Author",
                publisher = "Publisher",
                publishedDate = null,
                coverUrl = null,
                totalPages = null,
                price = null,
                category = null,
                tableOfContentsUrl = null,
                introductionUrl = null,
                summaryUrl = null,
            )
    }
}
