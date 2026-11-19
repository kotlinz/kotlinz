package com.github.kotlinz.kotlinz.data.statet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * State monad transformer: a function `S -> F<Pair<A, S>>` that threads state through an inner monad `F`.
 *
 * Use it when stateful steps also need another effect (failure via Maybe, side effects via IO, ...).
 * Has Functor, Applicative, Monad, MonadState and MonadTrans instances, all parameterised by the inner [Monad],
 * plus MFunctor.
 *
 * ```kotlin
 * val identity = object : IdentityMonad {}
 * val program = StateT.get<Identity.T, Int>(identity) bind { n ->
 *   StateT.put<Identity.T, Int>(identity, n + 1) fmap { n * 10 }
 * }
 * program.run(1) // Identity(Pair(10, 2))
 * ```
 *
 * @property monad the inner monad instance.
 * @property run the state transition function.
 */
class StateT<F, S, A>(val monad: Monad<F>, val run: (S) -> K1<F, Pair<A, S>>) : K3<StateT.T, F, S, A> {
    class T

    companion object {
        /** Downcasts a higher-kinded value to its concrete type. */
        fun <F, S, A> narrow(v: K1<K2<T, F, S>, A>): StateT<F, S, A> = v as StateT<F, S, A>

        // Monad
        /** Returns `v` without touching the state. */
        fun <F, S, A> pure(monad: Monad<F>, v: A): StateT<F, S, A> = instance<F, S>(monad).pure(v)
        /** Flattens a nested StateT. */
        fun <F, S, A> join(monad: Monad<F>, v: K1<K2<T, F, S>, K1<K2<T, F, S>, A>>): StateT<F, S, A> = instance<F, S>(monad).join(v)
        /** Builds a StateT using monad comprehension syntax. */
        fun <F, S, A> fx(monad: Monad<F>, block: suspend MonadScope<K2<T, F, S>>.() -> A): StateT<F, S, A> = narrow(instance<F, S>(monad).fx(block))

        // MonadState
        /** Returns the current state as the value. */
        fun <F, S> get(monad: Monad<F>): StateT<F, S, S> = instance<F, S>(monad).get()
        /** Replaces the state with `s`. */
        fun <F, S> put(monad: Monad<F>, s: S): StateT<F, S, Unit> = instance<F, S>(monad).put(s)
        /** Updates the state with `f`. */
        fun <F, S> modify(monad: Monad<F>, f: (S) -> S): StateT<F, S, Unit> = instance<F, S>(monad).modify(f)
        /** Returns a projection of the current state. */
        fun <F, S, A> gets(monad: Monad<F>, f: (S) -> A): StateT<F, S, A> = narrow(instance<F, S>(monad).gets(f))
        /** Wraps a pure state transition function. */
        fun <F, S, A> state(monad: Monad<F>, f: (S) -> Pair<A, S>): StateT<F, S, A> = instance<F, S>(monad).state(f)

        // Transformer
        /** Lifts an inner monadic value into StateT, leaving the state unchanged. */
        fun <F, S, A> lift(monad: Monad<F>, v: K1<F, A>): StateT<F, S, A> = instance<F, S>(monad).lift(v)

        private fun <F, S> instance(monad: Monad<F>) = object: StateTMonadState<F, S>, StateTMonadTrans<F, S> { override val monad = monad }
    }

    // Monad
    /** Maps the produced value. */
    infix fun <B> fmap(f: (A) -> B): StateT<F, S, B> = instance().fmap(f, this)
    /** Runs `f` then this, applying the resulting function to the resulting value. */
    infix fun <B> ap(f: StateT<F, S, (A) -> B>): StateT<F, S, B> = instance().ap(f, this)
    /** Sequences this computation with `f`, threading the state through the inner monad. */
    infix fun <B> bind(f: (A) -> StateT<F, S, B>): StateT<F, S, B> = instance().bind(f, this)

    /** Runs from initial state `s` and returns only the value inside `F`. */
    fun eval(s: S): K1<F, A> = monad.fmap({ pair: Pair<A, S> -> pair.first }, run(s))
    /** Runs from initial state `s` and returns only the final state inside `F`. */
    fun exec(s: S): K1<F, S> = monad.fmap({ pair: Pair<A, S> -> pair.second }, run(s))

    // MFunctor
    /** Swaps the base monad using [nt]; [target] is the monad of the new base. */
    fun <G> hoist(target: Monad<G>, nt: NaturalTransformation<F, G>): StateT<G, S, A> {
        return object: StateTMFunctor<F, G, S> { override val target = target }.hoist(nt, this)
    }

    private fun instance() = object: StateTMonadState<F, S> { override val monad = this@StateT.monad }
}
