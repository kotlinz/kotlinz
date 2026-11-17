package com.github.kotlinz.kotlinz.law

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.monad.MonadError
import io.kotest.matchers.shouldBe

class MonadErrorLaw<T, E>(val monad: MonadError<T, E>, val run: (K1<T, *>) -> Any? = { it }) {
    fun <A> raiseErrorHandleErrorWith(e: E, f: (E) -> K1<T, A>) {
        run(monad.handleErrorWith(f, monad.raiseError(e))) shouldBe run(f(e))
    }

    fun <A> handleErrorWithPure(a: A, f: (E) -> K1<T, A>) {
        run(monad.handleErrorWith(f, monad.pure(a))) shouldBe run(monad.pure(a))
    }

    fun <A> raiseErrorAttempt(e: E) {
        run(monad.attempt(monad.raiseError<A>(e))) shouldBe run(monad.pure(Either.Left<E, A>(e)))
    }

    fun <A> pureAttempt(a: A) {
        run(monad.attempt(monad.pure(a))) shouldBe run(monad.pure(Either.Right<E, A>(a)))
    }

    fun <A> raiseErrorBind(e: E, f: (A) -> K1<T, A>) {
        run(monad.bind(f, monad.raiseError(e))) shouldBe run(monad.raiseError<A>(e))
    }

    fun <A> assertSatisfyingMonadErrorLaw(e: E, a: A) {
        val f = { _: E -> monad.pure(a) }
        raiseErrorHandleErrorWith(e, f)
        handleErrorWithPure(a, f)
        raiseErrorAttempt<A>(e)
        pureAttempt(a)
        raiseErrorBind(e) { x: A -> monad.pure(x) }
    }
}
