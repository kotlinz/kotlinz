package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.ComonadEnv
import io.kotest.matchers.shouldBe

class ComonadEnvLaw<T, E>(val env: ComonadEnv<T, E>) {
    fun <A, B> askFmap(f: (A) -> B, v: K1<T, A>) {
        env.ask(env.fmap(f, v)) shouldBe env.ask(v)
    }

    fun <A, B> askExtend(f: (K1<T, A>) -> B, v: K1<T, A>) {
        env.ask(env.extend(f, v)) shouldBe env.ask(v)
    }

    fun <A, B> assertSatisfyingComonadEnvLaw(v: K1<T, A>, f: (A) -> B) {
        askFmap(f, v)
        askExtend({ w: K1<T, A> -> f(env.extract(w)) }, v)
    }
}
