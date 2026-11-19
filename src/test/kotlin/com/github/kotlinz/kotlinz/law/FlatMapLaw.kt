package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.FlatMap
import io.kotest.matchers.booleans.shouldBeTrue

class FlatMapLaw<T>(val flatMap: FlatMap<T>, val eq: (K1<T, *>, K1<T, *>) -> Boolean = { a, b -> a == b }) {
    fun <A, B, C> associativity(m: K1<T, A>, f: (A) -> K1<T, B>, g: (B) -> K1<T, C>): Boolean =
            eq(flatMap.bind(g, flatMap.bind(f, m)), flatMap.bind({ a: A -> flatMap.bind(g, f(a)) }, m))

    fun <A, B> apConsistency(mf: K1<T, (A) -> B>, m: K1<T, A>): Boolean =
            eq(flatMap.ap(mf, m), flatMap.bind({ f: (A) -> B -> flatMap.fmap(f, m) }, mf))

    fun <A> joinConsistency(mm: K1<T, K1<T, A>>): Boolean =
            eq(flatMap.join(mm), flatMap.bind({ m: K1<T, A> -> m }, mm))

    fun <A, B, C> assertSatisfyingFlatMapLaw(m: K1<T, A>, f: (A) -> K1<T, B>, g: (B) -> K1<T, C>, mf: K1<T, (A) -> B>) {
        associativity(m, f, g).shouldBeTrue()
        apConsistency(mf, m).shouldBeTrue()
        joinConsistency(flatMap.fmap(f, m)).shouldBeTrue()
    }
}
