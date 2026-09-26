package com.leeseungyun1020.manicule.feature.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class LicensesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeLibraries = listOf(
        OpenSourceLibrary(
            name = "Test Library",
            copyright = "Copyright Test Authors",
            license = "Apache License 2.0",
            url = "https://example.com",
        ),
    )
    private val fakeLicenseText = "Apache License Version 2.0"

    @Test
    fun loadLicenses_success_emitsLoadingThenSuccess() =
        runTest {
            val loader = FakeLicenseLoader(
                libraries = fakeLibraries,
                licenseText = fakeLicenseText,
            )
            val viewModel = LicensesViewModel(loader)

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(LicensesUiState.Loading)
                val success = awaitItem()
                assertThat(success).isEqualTo(
                    LicensesUiState.Success(
                        libraries = fakeLibraries,
                        licenseText = fakeLicenseText,
                    ),
                )
            }
        }

    @Test
    fun loadLicenses_librariesFailure_emitsError() =
        runTest {
            val loader = FakeLicenseLoader(
                librariesError = IOException("Failed to load libraries"),
                licenseText = fakeLicenseText,
            )
            val viewModel = LicensesViewModel(loader)

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(LicensesUiState.Loading)
                assertThat(awaitItem()).isEqualTo(LicensesUiState.Error)
            }
        }

    @Test
    fun loadLicenses_licenseTextFailure_emitsError() =
        runTest {
            val loader = FakeLicenseLoader(
                libraries = fakeLibraries,
                licenseTextError = IOException("Failed to load license text"),
            )
            val viewModel = LicensesViewModel(loader)

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(LicensesUiState.Loading)
                assertThat(awaitItem()).isEqualTo(LicensesUiState.Error)
            }
        }

    @Test
    fun retry_afterFailure_reloadsSuccessfully() =
        runTest {
            val loader = FakeLicenseLoader(
                librariesError = IOException("Initial failure"),
                licenseText = fakeLicenseText,
            )
            val viewModel = LicensesViewModel(loader)

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(LicensesUiState.Loading)
                assertThat(awaitItem()).isEqualTo(LicensesUiState.Error)

                loader.librariesError = null
                loader.libraries = fakeLibraries
                viewModel.retry()

                assertThat(awaitItem()).isEqualTo(LicensesUiState.Loading)
                assertThat(awaitItem()).isEqualTo(
                    LicensesUiState.Success(
                        libraries = fakeLibraries,
                        licenseText = fakeLicenseText,
                    ),
                )
            }
        }

    @Test
    fun defaultOpenSourceLicenseLoader_loadsAndParsesContent() =
        runTest {
            val jsonContent =
                """
                [
                  {
                    "name": "Library A",
                    "copyright": "Copyright A",
                    "license": "Apache License 2.0",
                    "url": "https://example.com/a"
                  }
                ]
                """.trimIndent()
            val textContent = "Apache License 2.0 Text Content"

            val loader = DefaultOpenSourceLicenseLoader(
                openRawResource = { id ->
                    when (id) {
                        R.raw.licenses -> ByteArrayInputStream(jsonContent.toByteArray())
                        R.raw.license_apache_2_0 -> ByteArrayInputStream(textContent.toByteArray())
                        else -> throw IllegalArgumentException("Unknown resource id: $id")
                    }
                },
                ioDispatcher = UnconfinedTestDispatcher(),
            )

            val libraries = loader.loadLibraries()
            val licenseText = loader.loadLicenseText()

            assertThat(libraries).hasSize(1)
            assertThat(libraries.first().name).isEqualTo("Library A")
            assertThat(libraries.first().copyright).isEqualTo("Copyright A")
            assertThat(libraries.first().license).isEqualTo("Apache License 2.0")
            assertThat(libraries.first().url).isEqualTo("https://example.com/a")
            assertThat(licenseText).isEqualTo(textContent)
        }
}

private class FakeLicenseLoader(
    var libraries: List<OpenSourceLibrary> = emptyList(),
    var licenseText: String = "",
    var librariesError: Throwable? = null,
    var licenseTextError: Throwable? = null,
) : OpenSourceLicenseLoader {
    override suspend fun loadLibraries(): List<OpenSourceLibrary> {
        librariesError?.let { throw it }
        return libraries
    }

    override suspend fun loadLicenseText(): String {
        licenseTextError?.let { throw it }
        return licenseText
    }
}
