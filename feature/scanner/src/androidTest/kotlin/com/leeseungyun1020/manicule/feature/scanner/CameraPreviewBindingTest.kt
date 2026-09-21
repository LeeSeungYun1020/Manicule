package com.leeseungyun1020.manicule.feature.scanner

import android.Manifest
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingData
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.domain.scanner.GetBookByScanUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.scanner.BarcodeReaderFactory
import com.leeseungyun1020.manicule.core.scanner.MlKitBarcodeReaderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CameraPreviewBindingTest {
    @get:Rule(order = 0)
    val permission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<ComponentActivity>()

    private fun provider(): ProcessCameraProvider =
        ProcessCameraProvider.getInstance(
            InstrumentationRegistry.getInstrumentation().targetContext,
        ).get(30, TimeUnit.SECONDS)

    @Test
    fun routeForwardsBackAndSearchCallbacksFromFailureScreen() {
        val viewModel =
            ScannerViewModel(BarcodeReaderFactory { error("Unavailable camera") }, scanUseCase(), Dispatchers.Default, SavedStateHandle())
        viewModel.onPermissionChanged(true)
        var back = false
        var search = false
        compose.setContent {
            ManiculeTheme { ScannerRoute({ back = true }, { search = true }, {}, viewModel) }
        }
        val context = compose.activity
        compose.onNodeWithText(context.getString(R.string.scanner_search)).performClick()
        compose.onNodeWithContentDescription(
            context.getString(com.leeseungyun1020.manicule.core.designsystem.R.string.core_designsystem_back),
        ).performClick()
        compose.runOnIdle {
            assertThat(back).isTrue()
            assertThat(search).isTrue()
        }
    }

    @Test
    fun bindingUsesDisplayRotationAndUnbindsOnlyOwnedUseCases() {
        val provider = provider()
        lateinit var binding: CameraXPreviewBinding
        lateinit var analysis: ImageAnalysis
        lateinit var other: ImageCapture
        lateinit var view: PreviewView
        compose.activityRule.scenario.onActivity { activity ->
            view = PreviewView(activity)
            activity.setContentView(view)
            analysis = ImageAnalysis.Builder().build()
            other = ImageCapture.Builder().build()
            provider.bindToLifecycle(activity, CameraSelector.DEFAULT_BACK_CAMERA, other)
            binding = CameraXPreviewBinding(provider, activity, view, analysis, { throw it })
            binding.bind()
            assertThat(provider.isBound(binding.preview)).isTrue()
            assertThat(provider.isBound(analysis)).isTrue()
            assertThat(analysis.targetRotation).isEqualTo(view.display.rotation)
            listOf(Surface.ROTATION_0, Surface.ROTATION_90, Surface.ROTATION_180, Surface.ROTATION_270).forEach {
                binding.applyDisplayRotation(it)
                assertThat(binding.preview.targetRotation).isEqualTo(it)
                assertThat(analysis.targetRotation).isEqualTo(it)
            }
        }
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.activityRule.scenario.onActivity {
            assertThat(analysis.targetRotation).isEqualTo(view.display.rotation)
            assertThat(binding.preview.targetRotation).isEqualTo(view.display.rotation)
            binding.close()
            assertThat(provider.isBound(analysis)).isFalse()
            assertThat(provider.isBound(binding.preview)).isFalse()
            assertThat(provider.isBound(other)).isTrue()
            provider.unbind(other)
        }
    }

    @Test
    fun activityRecreationRebindsSameViewModelReaderToNewLifecycle() {
        val provider = provider()
        lateinit var firstViewModel: ScannerViewModel
        lateinit var analysis: ImageAnalysis
        lateinit var firstBinding: CameraXPreviewBinding
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ScannerViewModel(MlKitBarcodeReaderFactory(), scanUseCase(), Dispatchers.Default, SavedStateHandle()) as T
        }
        compose.activityRule.scenario.onActivity { activity ->
            firstViewModel = ViewModelProvider(activity, factory)[ScannerViewModel::class.java]
            firstViewModel.onPermissionChanged(true)
            analysis = firstViewModel.reader!!.imageAnalysis
            val view = PreviewView(activity)
            activity.setContentView(view)
            firstBinding = CameraXPreviewBinding(provider, activity, view, analysis, { throw it })
            firstBinding.bind()
        }
        compose.activityRule.scenario.recreate()
        compose.activityRule.scenario.onActivity { activity ->
            val restored = ViewModelProvider(activity, factory)[ScannerViewModel::class.java]
            assertThat(restored).isSameInstanceAs(firstViewModel)
            assertThat(restored.reader!!.imageAnalysis).isSameInstanceAs(analysis)
            assertThat(provider.isBound(firstBinding.preview)).isFalse()
            val view = PreviewView(activity)
            activity.setContentView(view)
            val binding = CameraXPreviewBinding(provider, activity, view, analysis, { throw it })
            binding.bind()
            assertThat(provider.isBound(analysis)).isTrue()
            assertThat(analysis.targetRotation).isEqualTo(view.display.rotation)
            binding.close()
        }
    }

    private fun scanUseCase() = GetBookByScanUseCase(EmptyBookRepository())

    private class EmptyBookRepository : BookRepository {
        override fun observeBook(isbn: String): Flow<Book?> = emptyFlow()

        override suspend fun syncBook(isbn: String): Result<BookSyncStatus> = Result.failure(NoSuchElementException(isbn))

        override fun searchBooks(query: String): Flow<PagingData<Book>> = emptyFlow()
    }
}
