package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bifunctor
import io.kotest.matchers.booleans.shouldBeTrue

class BifunctorLaw<T>(
        val bifunctor: Bifunctor<T>,
        val eq: (K2<T, *, *>, K2<T, *, *>) -> Boolean = { a, b -> a == b }
) {
    fun <A, B> identity(v: K2<T, A, B>): Boolean =
            eq(bifunctor.bimap({ a: A -> a }, { b: B -> b }, v), v)

    fun <A, B> composition(v: K2<T, A, B>, f: (A) -> A, g: (A) -> A, h: (B) -> B, i: (B) -> B): Boolean =
            eq(
                    bifunctor.bimap({ a: A -> f(g(a)) }, { b: B -> h(i(b)) }, v),
                    bifunctor.bimap(f, h, bifunctor.bimap(g, i, v))
            )

    fun <A, B> bimapConsistency(v: K2<T, A, B>, f: (A) -> A, g: (B) -> B): Boolean =
            eq(bifunctor.bimap(f, g, v), bifunctor.first(f, bifunctor.second(g, v)))

    fun <A, B> assertSatisfyingBifunctorLaw(v: K2<T, A, B>, f: (A) -> A, g: (A) -> A, h: (B) -> B, i: (B) -> B) {
        identity(v).shouldBeTrue()
        composition(v, f, g, h, i).shouldBeTrue()
        bimapConsistency(v, f, h).shouldBeTrue()
    }
}
