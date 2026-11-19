package com.github.kotlinz.kotlinz.data.selectt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.monad.Monad

/**
 * Selection monad transformer: `((A) -> F<R>) -> F<A>`, a choice of an `A` given a way to rank
 * each candidate by an outcome `R` in the inner monad `F`.
 *
 * Use it for search and game-playing problems where later choices are made knowing how they affect
 * the final outcome (e.g. greedy optimal play, argmax over sequences). `bind` runs continuations
 * several times. Instances: [SelectTFunctor], [SelectTApplicative], [SelectTMonad], [SelectTMonadTrans],
 * all given the inner [Monad]. See [com.github.kotlinz.kotlinz.data.select.Select] for the version without `F`.
 *
 * ```kotlin
 * val maybe = object : MaybeMonad {}
 * val pick = SelectT<Int, Maybe.T, Int>(maybe) { k -> Maybe.Just(listOf(1, 2, 3).maxBy { (k(it) as Maybe.Just).value }) }
 * pick.run { Maybe.Just(-it * it) } // Just(1)
 * ```
 *
 * @property monad the inner monad instance.
 * @property run the selection function.
 */
class SelectT<R, F, A>(val monad: Monad<F>, val run: ((A) -> K1<F, R>) -> K1<F, A>) : K3<SelectT.T, R, F, A> {
    /** Witness type used to encode `SelectT` as a higher-kinded `K3<SelectT.T, R, F, A>`. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [SelectT]. */
        fun <R, F, A> narrow(v: K1<K2<T, R, F>, A>): SelectT<R, F, A> = v as SelectT<R, F, A>

        // Monad
        /** Selects `v` regardless of its outcome. */
        fun <R, F, A> pure(monad: Monad<F>, v: A): SelectT<R, F, A> = instance<R, F>(monad).pure(v)
        /** Flattens a nested `SelectT`. */
        fun <R, F, A> join(monad: Monad<F>, v: K1<K2<T, R, F>, K1<K2<T, R, F>, A>>): SelectT<R, F, A> = instance<R, F>(monad).join(v)
        /** Builds a selection with monad comprehension syntax; the block is replayed per bound value. */
        fun <R, F, A> fx(monad: Monad<F>, block: suspend MonadScope<K2<T, R, F>>.() -> A): SelectT<R, F, A> =
            narrow(instance<R, F>(monad).fxReplay(block))

        // Transformer
        /** Lifts an inner monadic value, ignoring the ranking. */
        fun <R, F, A> lift(monad: Monad<F>, v: K1<F, A>): SelectT<R, F, A> = instance<R, F>(monad).lift(v)

        private fun <R, F> instance(monad: Monad<F>) = SelectTMonadTrans.of<R, F>(monad)
    }

    // Monad
    /** Maps the selected value; the ranking sees the mapped value. */
    infix fun <B> fmap(f: (A) -> B): SelectT<R, F, B> = instance().fmap(f, this)
    /** Selects a function, then a value for it. */
    infix fun <B> ap(f: SelectT<R, F, (A) -> B>): SelectT<R, F, B> = instance().ap(f, this)
    /** Sequences this selection with [f], choosing knowing how [f] continues. */
    infix fun <B> bind(f: (A) -> SelectT<R, F, B>): SelectT<R, F, B> = instance().bind(f, this)

    private fun instance() = SelectTMonadTrans.of<R, F>(monad)
}
