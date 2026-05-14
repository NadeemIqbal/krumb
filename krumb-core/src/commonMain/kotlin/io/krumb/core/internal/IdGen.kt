package io.krumb.core.internal

import kotlin.random.Random

internal object IdGen {
    fun next(): String {
        val a = Random.nextLong().toULong().toString(16).padStart(16, '0')
        val b = Random.nextInt().toUInt().toString(16).padStart(8, '0')
        return "t_${a}_$b"
    }
}
