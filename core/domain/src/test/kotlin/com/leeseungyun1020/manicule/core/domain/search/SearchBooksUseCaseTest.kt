package com.leeseungyun1020.manicule.core.domain.search

import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchBooksUseCaseTest {

    private class TrackingBookRepository : BookRepository {
        var lastSearchQuery: String? = null
        var lastSearchIsbn: String? = null

        override fun observeBook(isbn: String): Flow<Book?> = TODO("Not needed for test")

        override suspend fun syncBook(isbn: String): Result<BookSyncStatus> = TODO("Not needed for test")

        override fun searchBooks(query: String): Flow<PagingData<Book>> {
            lastSearchQuery = query
            return flowOf(PagingData.empty())
        }

        override fun searchBooksByIsbn(isbn: String): Flow<PagingData<Book>> {
            lastSearchIsbn = isbn
            return flowOf(PagingData.empty())
        }
    }

    private val trackingRepository = TrackingBookRepository()
    private val useCase = SearchBooksUseCase(trackingRepository)

    @Test
    fun invoke_withValidIsbn13WithHyphens_routesToSearchBooksByIsbnWithNormalizedValue() =
        runTest {
            useCase("978-89-546-9991-4")

            assertThat(trackingRepository.lastSearchIsbn).isEqualTo("9788954699914")
            assertThat(trackingRepository.lastSearchQuery).isNull()
        }

    @Test
    fun invoke_withValidIsbn10WithSpacesAndLowerX_routesToSearchBooksByIsbnWithNormalizedValue() =
        runTest {
            useCase("0 8044 2957 x")

            assertThat(trackingRepository.lastSearchIsbn).isEqualTo("080442957X")
            assertThat(trackingRepository.lastSearchQuery).isNull()
        }

    @Test
    fun invoke_withPlainTitleOrAuthor_routesToSearchBooks() =
        runTest {
            useCase("1984")

            assertThat(trackingRepository.lastSearchQuery).isEqualTo("1984")
            assertThat(trackingRepository.lastSearchIsbn).isNull()
        }

    @Test
    fun invoke_withInvalidChecksumNumber_routesToSearchBooks() =
        runTest {
            useCase("1234567890")

            assertThat(trackingRepository.lastSearchQuery).isEqualTo("1234567890")
            assertThat(trackingRepository.lastSearchIsbn).isNull()
        }

    @Test
    fun invoke_delegates_to_repository_searchBooks() =
        runTest {
            val resultFlow = useCase("Kotlin")

            assertThat(trackingRepository.lastSearchQuery).isEqualTo("Kotlin")
            resultFlow.collect { pagingData ->
                assertThat(pagingData).isNotNull()
            }
        }
}
