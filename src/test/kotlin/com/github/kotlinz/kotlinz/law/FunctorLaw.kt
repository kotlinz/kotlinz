package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import io.kotest.matchers.booleans.shouldBeTrue

class FunctorLaw<T>(val functor: Functor<T>, val eq: (K1<T, *>, K1<T, *>) -> Boolean = { a, b -> a == b }) {
    fun <A> identity(v: K1<T, A>): Boolean =
            eq(functor.fmap({ a: A -> a }, v), v)

    fun <A, B, C> composition(v: K1<T, A>, f: (A) -> B, g: (B) -> C): Boolean =
            eq(functor.fmap({ a: A -> g(f(a)) }, v), functor.fmap(g, functor.fmap(f, v)))

    fun <A, B, C> assertSatisfyingFunctorLaw(v: K1<T, A>, f: (A) -> B, g: (B) -> C) {
        identity(v).shouldBeTrue()
        composition(v, f, g).shouldBeTrue()
    }
}
