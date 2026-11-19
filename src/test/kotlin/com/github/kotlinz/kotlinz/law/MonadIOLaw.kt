package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.type.monad.MonadIO
import io.kotest.matchers.shouldBe

class MonadIOLaw<T>(val monad: MonadIO<T>, val run: (K1<T, *>) -> Any?) {
    fun <A> liftPure(a: A) {
        run(monad.liftIO(IO.pure(a))) shouldBe run(monad.pure(a))
    }

    fun <A, B> liftBind(io: IO<A>, f: (A) -> IO<B>) {
        run(monad.liftIO(io bind f)) shouldBe run(monad.bind({ a -> monad.liftIO(f(a)) }, monad.liftIO(io)))
    }

    fun <A, B> assertSatisfyingMonadIOLaw(a: A, io: IO<A>, f: (A) -> IO<B>) {
        liftPure(a)
        liftBind(io, f)
    }
}
