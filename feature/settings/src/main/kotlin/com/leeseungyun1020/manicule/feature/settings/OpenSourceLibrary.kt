package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class OpenSourceLibrary(
    val name: String,
    val copyright: String,
    val license: String,
    val licenseUrl: String,
    val url: String? = null,
)
