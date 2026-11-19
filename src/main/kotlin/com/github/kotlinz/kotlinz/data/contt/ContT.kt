package com.github.kotlinz.kotlinz.data.contt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.monad.MonadIO

/**
 * Continuation monad transformer: a computation `((A) -> F<R>) -> F<R>` whose continuation runs in the monad `F`.
 *
 * Use it for early exit ([callCC]), delimited control ([reset] / [shift]) or CPS code that also performs
 * effects of `F`. Instances: [ContTFunctor], [ContTApplicative], [ContTMonad], [ContTMonadCont],
 * [ContTMonadTrans] and [ContTMonadIO]. Not stack-safe: deeply nested continuations may overflow the stack.
 *
 * ```kotlin
 * val identity = object : IdentityMonad {}
 * val program = ContT.callCC<Int, Identity.T, Int, Int>(identity) { exit ->
 *     exit(10) bind { ContT.pure(identity, it + 100) }
 * }
 * program.eval() // Identity(10)
 * ```
 *
 * @property monad the underlying monad instance.
 * @property run the computation, given the continuation of its result.
 */
class ContT<R, F, A>(val monad: Monad<F>, val run: ((A) -> K1<F, R>) -> K1<F, R>) : K3<ContT.T, R, F, A> {
    /** Higher-kinded witness tag for [ContT]. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [ContT]. */
        fun <R, F, A> narrow(v: K1<K2<T, R, F>, A>): ContT<R, F, A> = v as ContT<R, F, A>

        // Monad
        /** Passes [v] straight to the continuation. */
        fun <R, F, A> pure(monad: Monad<F>, v: A): ContT<R, F, A> = instance<R, F>(monad).pure(v)
        /** Flattens a nested [ContT]. */
        fun <R, F, A> join(monad: Monad<F>, v: K1<K2<T, R, F>, K1<K2<T, R, F>, A>>): ContT<R, F, A> = instance<R, F>(monad).join(v)
        /** Runs a monad comprehension block in [ContT]; continuations must be resumed at most once. */
        fun <R, F, A> fx(monad: Monad<F>, block: suspend MonadScope<K2<T, R, F>>.() -> A): ContT<R, F, A> = narrow(instance<R, F>(monad).fx(block))

        // MonadCont
        /** Call with current continuation: the escape function aborts the block, which returns its argument. */
        fun <R, F, A, B> callCC(monad: Monad<F>, f: ((A) -> ContT<R, F, B>) -> ContT<R, F, A>): ContT<R, F, A> {
            return instance<R, F>(monad).callCC { k: (A) -> K1<K2<T, R, F>, B> -> f { a -> narrow(k(a)) } }
        }

        // Delimited control
        /** Delimits the continuations captured by [shift] inside [v], running it to a value in `F`. */
        fun <R, R2, F> reset(monad: Monad<F>, v: ContT<R, F, R>): ContT<R2, F, R> = lift(monad, v.eval())
        /** Captures the continuation up to the nearest [reset] as a function `(A) -> F<R>` passed to [f]. */
        fun <R, F, A> shift(monad: Monad<F>, f: ((A) -> K1<F, R>) -> ContT<R, F, R>): ContT<R, F, A> = ContT(monad) { k -> f(k).eval() }

        // Transformer
        /** Lifts an inner computation by binding the continuation to it. */
        fun <R, F, A> lift(monad: Monad<F>, v: K1<F, A>): ContT<R, F, A> = instance<R, F>(monad).lift(v)
        /** Lifts an [IO] computation through the base monad's MonadIO instance. */
        fun <R, F, A> liftIO(monad: MonadIO<F>, v: IO<A>): ContT<R, F, A> = object: ContTMonadIO<R, F> { override val monad = monad }.liftIO(v)

        private fun <R, F> instance(monad: Monad<F>) = object: ContTMonadCont<R, F>, ContTMonadTrans<R, F> { override val monad = monad }
    }

    // Monad
    /** Maps the result before it reaches the continuation. */
    infix fun <B> fmap(f: (A) -> B): ContT<R, F, B> = instance().fmap(f, this)
    /** Applies the function produced by [f] to the result. */
    infix fun <B> ap(f: ContT<R, F, (A) -> B>): ContT<R, F, B> = instance().ap(f, this)
    /** Sequences a dependent computation. */
    infix fun <B> bind(f: (A) -> ContT<R, F, B>): ContT<R, F, B> = instance().bind(f, this)

    private fun instance() = instance<R, F>(monad)
}

/** Runs the computation with `pure` as the final continuation. */
fun <R, F> ContT<R, F, R>.eval(): K1<F, R> = run { monad.pure(it) }
