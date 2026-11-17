package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Alternative
import io.kotest.matchers.booleans.shouldBeTrue

class AlternativeLaw<T>(val alternative: Alternative<T>) {
    fun <V> leftIdentity(v: K1<T, V>): Boolean =
            alternative.orElse(alternative.empty(), v) == v

    fun <V> rightIdentity(v: K1<T, V>): Boolean =
            alternative.orElse(v, alternative.empty()) == v

    fun <V> associativity(a: K1<T, V>, b: K1<T, V>, c: K1<T, V>): Boolean =
            alternative.orElse(alternative.orElse(a, b), c) == alternative.orElse(a, alternative.orElse(b, c))

    fun <V> assertSatisfyingAlternativeLaw(a: K1<T, V>, b: K1<T, V>, c: K1<T, V>) {
        leftIdentity(a).shouldBeTrue()
        rightIdentity(a).shouldBeTrue()
        associativity(a, b, c).shouldBeTrue()
    }
}
