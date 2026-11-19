package com.github.kotlinz.kotlinz.data.indexedstatet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.monad.Monad

/**
 * Indexed state monad transformer: `I -> F<Pair<A, O>>`, an [com.github.kotlinz.kotlinz.data.indexedstate.IndexedState]
 * whose steps also run in an inner monad `F`.
 *
 * Encoded as `K3<K1<IndexedStateT.T, F>, I, O, A>` (the inner monad is part of the tag) so that it fits
 * [com.github.kotlinz.kotlinz.type.monad.IxMonad]. Instances: [IndexedStateTIxFunctor],
 * [IndexedStateTIxApplicative], [IndexedStateTIxMonad], all given the inner [Monad].
 *
 * ```kotlin
 * val identity = object : IdentityMonad {}
 * val program = IndexedStateT.get<Identity.T, Int>(identity) bind { n -> IndexedStateT.put<Identity.T, Int, String>(identity, "n=$n") }
 * program.run(1) // Identity(Pair(Unit, "n=1"))
 * ```
 *
 * @property monad the inner monad instance.
 * @property run the state transition function.
 */
class IndexedStateT<F, I, O, A>(val monad: Monad<F>, val run: (I) -> K1<F, Pair<A, O>>) : K3<K1<IndexedStateT.T, F>, I, O, A> {
    class T

    companion object {
        /** Downcasts a higher-kinded value to its concrete type. */
        fun <F, I, O, A> narrow(v: K1<K2<K1<T, F>, I, O>, A>): IndexedStateT<F, I, O, A> = v as IndexedStateT<F, I, O, A>

        /** Returns `v` without touching the state. */
        fun <F, S, A> pure(monad: Monad<F>, v: A): IndexedStateT<F, S, S, A> = IndexedStateT(monad) { s -> monad.pure(Pair(v, s)) }
        /** Returns the current state as the value. */
        fun <F, S> get(monad: Monad<F>): IndexedStateT<F, S, S, S> = IndexedStateT(monad) { s -> monad.pure(Pair(s, s)) }
        /** Replaces the state with `o`, possibly of a different type. */
        fun <F, I, O> put(monad: Monad<F>, o: O): IndexedStateT<F, I, O, Unit> = IndexedStateT(monad) { monad.pure(Pair(Unit, o)) }
        /** Transforms the state with `f`, possibly changing its type. */
        fun <F, I, O> modify(monad: Monad<F>, f: (I) -> O): IndexedStateT<F, I, O, Unit> = IndexedStateT(monad) { i -> monad.pure(Pair(Unit, f(i))) }

        // Transformer
        /** Lifts an inner monadic value, leaving the state unchanged. */
        fun <F, S, A> lift(monad: Monad<F>, v: K1<F, A>): IndexedStateT<F, S, S, A> = IndexedStateT(monad) { s -> monad.fmap({ a: A -> Pair(a, s) }, v) }
    }

    /** Maps the produced value. */
    infix fun <B> fmap(f: (A) -> B): IndexedStateT<F, I, O, B> = instance().imap(f, this)
    /** Runs `f`, then this, applying the resulting function to the resulting value. */
    infix fun <I0, B> ap(f: IndexedStateT<F, I0, I, (A) -> B>): IndexedStateT<F, I0, O, B> = instance().iap(f, this)
    /** Sequences this computation with `f`, whose input state is this computation's output state. */
    infix fun <O2, B> bind(f: (A) -> IndexedStateT<F, O, O2, B>): IndexedStateT<F, I, O2, B> = instance().ibind(f, this)

    /** Runs from initial state `i` and returns only the value inside `F`. */
    fun eval(i: I): K1<F, A> = monad.fmap({ pair: Pair<A, O> -> pair.first }, run(i))
    /** Runs from initial state `i` and returns only the final state inside `F`. */
    fun exec(i: I): K1<F, O> = monad.fmap({ pair: Pair<A, O> -> pair.second }, run(i))

    private fun instance() = IndexedStateTIxMonad.of(monad)
}
