package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class OpenSourceLibrary(
    val name: String,
    val copyright: String,
    val license: String = "Apache License 2.0",
    val url: String? = null,
)
