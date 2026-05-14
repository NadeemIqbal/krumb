package io.krumb.core.internal

import io.krumb.core.ToastType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal object DefaultDurations {
    val SUCCESS: Duration = 4.seconds
    val INFO: Duration = 4.seconds
    val WARNING: Duration = 5.seconds
    val ERROR: Duration = 6.seconds
    val LOADING: Duration = Duration.INFINITE
    val CUSTOM: Duration = Duration.INFINITE

    fun forType(type: ToastType): Duration = when (type) {
        ToastType.Success -> SUCCESS
        ToastType.Info -> INFO
        ToastType.Warning -> WARNING
        ToastType.Error -> ERROR
        ToastType.Loading -> LOADING
        ToastType.Custom -> CUSTOM
    }
}
