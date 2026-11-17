package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadState
import io.kotest.matchers.shouldBe

class MonadStateLaw<T, S>(val monad: MonadState<T, S>, val run: (K1<T, *>) -> Any?) {
    fun getPut() {
        run(monad.bind({ s -> monad.put(s) }, monad.get())) shouldBe run(monad.pure(Unit))
    }

    fun putGet(s: S) {
        run(monad.bind({ _: Unit -> monad.get() }, monad.put(s))) shouldBe run(monad.bind({ _: Unit -> monad.pure(s) }, monad.put(s)))
    }

    fun putPut(s1: S, s2: S) {
        run(monad.bind({ _: Unit -> monad.put(s2) }, monad.put(s1))) shouldBe run(monad.put(s2))
    }

    fun getGet() {
        val twice = monad.bind({ s1 -> monad.fmap({ s2 -> Pair(s1, s2) }, monad.get()) }, monad.get())
        run(twice) shouldBe run(monad.fmap({ s -> Pair(s, s) }, monad.get()))
    }

    fun <A> stateDefinition(f: (S) -> Pair<A, S>) {
        val viaGetPut = monad.bind({ s -> f(s).let { (a, s2) -> monad.fmap({ _: Unit -> a }, monad.put(s2)) } }, monad.get())
        run(monad.state(f)) shouldBe run(viaGetPut)
    }

    fun modifyDefinition(f: (S) -> S) {
        run(monad.modify(f)) shouldBe run(monad.bind({ s -> monad.put(f(s)) }, monad.get()))
    }

    fun assertSatisfyingMonadStateLaw(s1: S, s2: S, f: (S) -> S) {
        getPut()
        putGet(s1)
        putPut(s1, s2)
        getGet()
        stateDefinition { s -> Pair(s, f(s)) }
        modifyDefinition(f)
    }
}
