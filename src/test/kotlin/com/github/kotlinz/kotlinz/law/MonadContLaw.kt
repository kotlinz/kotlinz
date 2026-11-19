package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadCont
import io.kotest.matchers.booleans.shouldBeTrue

class MonadContLaw<T>(val monad: MonadCont<T>, val eq: (K1<T, *>, K1<T, *>) -> Boolean = { a, b -> a == b }) {
    fun <A, B> escape(a: A, f: (B) -> K1<T, A>): Boolean =
            eq(monad.callCC { k: (A) -> K1<T, B> -> monad.bind(f, k(a)) }, monad.pure(a))

    fun <A> unusedContinuation(m: K1<T, A>): Boolean =
            eq(monad.callCC { _: (A) -> K1<T, Nothing> -> m }, m)

    fun <A> returning(a: A): Boolean =
            eq(monad.callCC { k: (A) -> K1<T, A> -> k(a) }, monad.pure(a))

    fun <A> assertSatisfyingMonadContLaw(a: A, m: K1<T, A>, f: (A) -> K1<T, A>) {
        escape(a, f).shouldBeTrue()
        unusedContinuation(m).shouldBeTrue()
        returning(a).shouldBeTrue()
    }
}
