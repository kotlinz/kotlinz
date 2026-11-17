package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadReader
import io.kotest.matchers.shouldBe

class MonadReaderLaw<T, R>(val monad: MonadReader<T, R>, val run: (K1<T, *>) -> Any?) {
    fun <A> askDiscarded(v: K1<T, A>) {
        run(monad.bind({ _: R -> v }, monad.ask())) shouldBe run(v)
    }

    fun <A> localIdentity(v: K1<T, A>) {
        run(monad.local({ it }, v)) shouldBe run(v)
    }

    fun <A> localComposition(f: (R) -> R, g: (R) -> R, v: K1<T, A>) {
        run(monad.local(f, monad.local(g, v))) shouldBe run(monad.local({ g(f(it)) }, v))
    }

    fun localAsk(f: (R) -> R) {
        run(monad.local(f, monad.ask())) shouldBe run(monad.fmap(f, monad.ask()))
    }

    fun <A> readerAsk(f: (R) -> A) {
        run(monad.reader(f)) shouldBe run(monad.fmap(f, monad.ask()))
    }

    fun <A> assertSatisfyingMonadReaderLaw(v: K1<T, A>, f: (R) -> R, g: (R) -> R) {
        askDiscarded(v)
        localIdentity(v)
        localComposition(f, g, v)
        localAsk(f)
        readerAsk(g)
    }
}
