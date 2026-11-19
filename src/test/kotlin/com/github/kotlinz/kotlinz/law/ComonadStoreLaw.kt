package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.ComonadStore
import io.kotest.matchers.booleans.shouldBeTrue

class ComonadStoreLaw<T, S>(val store: ComonadStore<T, S>, val eq: (Any?, Any?) -> Boolean = { a, b -> a == b }) {
    fun <A> peekPos(v: K1<T, A>): Boolean = eq(store.peek(store.pos(v), v), store.extract(v))

    fun <A> posSeek(s: S, v: K1<T, A>): Boolean = store.pos(store.seek(s, v)) == s

    fun <A> seekPos(v: K1<T, A>): Boolean = eq(store.seek(store.pos(v), v), v)

    fun <A> assertSatisfyingComonadStoreLaw(s: S, v: K1<T, A>) {
        peekPos(v).shouldBeTrue()
        posSeek(s, v).shouldBeTrue()
        seekPos(v).shouldBeTrue()
    }
}
