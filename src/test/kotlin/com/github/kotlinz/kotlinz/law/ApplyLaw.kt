package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Apply
import io.kotest.matchers.booleans.shouldBeTrue

class ApplyLaw<T>(val apply: Apply<T>, val eq: (K1<T, *>, K1<T, *>) -> Boolean = { a, b -> a == b }) {
    fun <A, B, C> associativeComposition(u: K1<T, (B) -> C>, v: K1<T, (A) -> B>, w: K1<T, A>): Boolean {
        val compose = { g: (B) -> C -> { f: (A) -> B -> { a: A -> g(f(a)) } } }
        val left = apply.ap(apply.ap(apply.fmap(compose, u), v), w)
        return eq(left, apply.ap(u, apply.ap(v, w)))
    }

    fun <A, B, C> assertSatisfyingApplyLaw(u: K1<T, (B) -> C>, v: K1<T, (A) -> B>, w: K1<T, A>) {
        associativeComposition(u, v, w).shouldBeTrue()
    }
}
