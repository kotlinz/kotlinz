package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad
import io.kotest.matchers.shouldBe

class ObservedMonadLaw<T>(val monad: Monad<T>, val run: (K1<T, *>) -> Any?) {
    fun <A, B> leftIdentity(a: A, f: (A) -> K1<T, B>) {
        run(monad.bind(f, monad.pure(a))) shouldBe run(f(a))
    }

    fun <A> rightIdentity(v: K1<T, A>) {
        run(monad.bind({ monad.pure(it) }, v)) shouldBe run(v)
    }

    fun <A, B, C> associativity(v: K1<T, A>, f: (A) -> K1<T, B>, g: (B) -> K1<T, C>) {
        run(monad.bind(g, monad.bind(f, v))) shouldBe run(monad.bind({ a -> monad.bind(g, f(a)) }, v))
    }

    fun <A> assertSatisfyingMonadLaw(a: A, v: K1<T, A>, f: (A) -> K1<T, A>, g: (A) -> K1<T, A>) {
        leftIdentity(a, f)
        rightIdentity(v)
        associativity(v, f, g)
    }
}
