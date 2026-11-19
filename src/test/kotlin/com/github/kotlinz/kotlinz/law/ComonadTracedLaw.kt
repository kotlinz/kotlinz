package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.ComonadTraced
import io.kotest.matchers.shouldBe

class ComonadTracedLaw<T, M>(val traced: ComonadTraced<T, M>, val empty: M, val combine: (M, M) -> M) {
    fun <A> traceEmpty(v: K1<T, A>) {
        traced.trace(empty, v) shouldBe traced.extract(v)
    }

    fun <A> traceExtend(m1: M, m2: M, v: K1<T, A>) {
        traced.trace(m2, traced.extend({ w: K1<T, A> -> traced.trace(m1, w) }, v)) shouldBe traced.trace(combine(m2, m1), v)
    }

    fun <A> assertSatisfyingComonadTracedLaw(m1: M, m2: M, v: K1<T, A>) {
        traceEmpty(v)
        traceExtend(m1, m2, v)
    }
}
