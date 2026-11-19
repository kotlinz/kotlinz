package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.monad.MonadTrans
import io.kotest.matchers.shouldBe

class MonadTransLaw<F, T>(val base: Monad<F>, val trans: MonadTrans<F, T>, val run: (K1<T, *>) -> Any?) {
    fun <A> liftPure(a: A) {
        run(trans.lift(base.pure(a))) shouldBe run(trans.pure(a))
    }

    fun <A, B> liftBind(m: K1<F, A>, f: (A) -> K1<F, B>) {
        run(trans.lift(base.bind(f, m))) shouldBe run(trans.bind({ a -> trans.lift(f(a)) }, trans.lift(m)))
    }

    fun <A, B> assertSatisfyingMonadTransLaw(a: A, m: K1<F, A>, f: (A) -> K1<F, B>) {
        liftPure(a)
        liftBind(m, f)
    }
}
