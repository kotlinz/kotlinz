package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Comonad
import io.kotest.matchers.booleans.shouldBeTrue

class ComonadLaw<T>(val comonad: Comonad<T>, val eq: (Any?, Any?) -> Boolean = { a, b -> a == b }) {
    fun <V> leftIdentity(v: K1<T, V>): Boolean =
            eq(comonad.extract(comonad.duplicate(v)), v)

    fun <V> rightIdentity(v: K1<T, V>): Boolean =
            eq(comonad.fmap({ w: K1<T, V> -> comonad.extract(w) }, comonad.duplicate(v)), v)

    fun <V> associativity(v: K1<T, V>): Boolean =
            eq(comonad.duplicate(comonad.duplicate(v)), comonad.fmap({ w: K1<T, V> -> comonad.duplicate(w) }, comonad.duplicate(v)))

    fun <V> assertSatisfyingComonadLaw(v: K1<T, V>) {
        leftIdentity(v).shouldBeTrue()
        rightIdentity(v).shouldBeTrue()
        associativity(v).shouldBeTrue()
    }
}
