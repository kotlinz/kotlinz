package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad
import io.kotest.matchers.booleans.shouldBeTrue

class MonadLaw<T>(val monad: Monad<T>) {
    fun <V> leftIdentity(v: V, f: (V) -> K1<T, V>): Boolean =
            monad.bind(f, monad.pure(v)) == f(v)

    fun <V> rightIdentity(v: K1<T, V>): Boolean {
        return monad.bind({ monad.pure(it) }, v) == v
    }

    fun <V> associativity(v: K1<T, V>, f: (V) -> K1<T, V>, g: (V) -> K1<T, V>): Boolean =
            monad.bind(g, monad.bind(f, v)) == monad.bind({ monad.bind(g, f(it)) }, v)

    fun <T> assertSatisfyingMonadLaw(v: T) {
        val mv = monad.pure(v)
        val f = { m: T -> monad.pure(m) }
        val g = { m: T -> monad.pure(m) }

        rightIdentity(mv).shouldBeTrue()
        leftIdentity(v, f).shouldBeTrue()
        associativity(mv, f, g).shouldBeTrue()
    }
}

