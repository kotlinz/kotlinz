package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Comonad
import com.github.kotlinz.kotlinz.type.monad.ComonadTrans
import io.kotest.matchers.booleans.shouldBeTrue

class ComonadTransLaw<W, T>(val base: Comonad<W>, val trans: ComonadTrans<W, T>, val eq: (Any?, Any?) -> Boolean = { a, b -> a == b }) {
    fun <A> extractLower(v: K1<T, A>): Boolean = eq(base.extract(trans.lower(v)), trans.extract(v))

    fun <A, B> lowerExtend(f: (K1<W, A>) -> B, v: K1<T, A>): Boolean =
        eq(trans.lower(trans.extend({ w: K1<T, A> -> f(trans.lower(w)) }, v)), base.extend(f, trans.lower(v)))

    fun <A, B> assertSatisfyingComonadTransLaw(v: K1<T, A>, f: (K1<W, A>) -> B) {
        extractLower(v).shouldBeTrue()
        lowerExtend(f, v).shouldBeTrue()
    }
}
