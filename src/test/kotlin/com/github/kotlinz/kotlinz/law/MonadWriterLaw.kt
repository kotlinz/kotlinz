package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadWriter
import io.kotest.matchers.shouldBe

class MonadWriterLaw<T, W>(
    val monad: MonadWriter<T, W>,
    val empty: W,
    val combine: (W, W) -> W,
    val run: (K1<T, *>) -> Any?
) {
    fun tellEmpty() {
        run(monad.tell(empty)) shouldBe run(monad.pure(Unit))
    }

    fun tellTell(w1: W, w2: W) {
        run(monad.bind({ _: Unit -> monad.tell(w2) }, monad.tell(w1))) shouldBe run(monad.tell(combine(w1, w2)))
    }

    fun <A> listenPure(a: A) {
        run(monad.listen(monad.pure(a))) shouldBe run(monad.pure(Pair(a, empty)))
    }

    fun <A> listenWriter(a: A, w: W) {
        run(monad.listen(monad.writer(Pair(a, w)))) shouldBe run(monad.writer(Pair(Pair(a, w), w)))
    }

    fun <A> passWriter(a: A, w: W, f: (W) -> W) {
        run(monad.pass(monad.writer(Pair(Pair(a, f), w)))) shouldBe run(monad.writer(Pair(a, f(w))))
    }

    fun <A> assertSatisfyingMonadWriterLaw(a: A, w1: W, w2: W) {
        tellEmpty()
        tellTell(w1, w2)
        listenPure(a)
        listenWriter(a, w1)
        passWriter(a, w1) { combine(it, w2) }
    }
}
