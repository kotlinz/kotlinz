package com.github.kotlinz.kotlinz.data.freet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fxReplay
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * The free monad transformer: programs built from instructions in the functor `F` interleaved with
 * effects of the inner monad `M`, represented as `M<FreeF<F, A, FreeT<F, M, A>>>`.
 *
 * Use it when a DSL's interpretation needs another effect at every step, or when instructions and
 * inner effects should be mixed in one program. Unlike `Free`, `F` must be a [Functor].
 * Instances: [FreeTFunctor], [FreeTApplicative], [FreeTMonad], [FreeTMonadTrans] (all given `Functor<F>` and
 * `Monad<M>`) and [FreeTMFunctor].
 * Deep programs are not stack-safe; that depends on the inner monad.
 *
 * ```kotlin
 * val program = FreeT.liftF(say, state, Say("hi", Unit)) bind { FreeT.lift(say, state, State.get<Int>()) }
 * program.iterT { s -> State.modify<Int> { it + 1 } bind { Say.narrow(s).next } }
 * ```
 *
 * @property functor the instruction functor.
 * @property monad the inner monad.
 * @property run the first layer of the program inside `M`.
 */
class FreeT<F, M, A>(val functor: Functor<F>, val monad: Monad<M>, val run: K1<M, FreeF<F, A, FreeT<F, M, A>>>) : K3<FreeT.T, F, M, A> {
    /** Witness type used to encode `FreeT` as a higher-kinded `K3<FreeT.T, F, M, A>`. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [FreeT]. */
        fun <F, M, A> narrow(v: K1<K2<T, F, M>, A>): FreeT<F, M, A> = v as FreeT<F, M, A>

        // Monad
        /** Wraps a value into a program with no instructions or effects. */
        fun <F, M, A> pure(functor: Functor<F>, monad: Monad<M>, v: A): FreeT<F, M, A> = instance(functor, monad).pure(v)
        /** Flattens a nested program. */
        fun <F, M, A> join(functor: Functor<F>, monad: Monad<M>, v: K1<K2<T, F, M>, K1<K2<T, F, M>, A>>): FreeT<F, M, A> = instance(functor, monad).join(v)
        /** Builds a program with monad comprehension syntax; the block is replayed per bound value. */
        fun <F, M, A> fx(functor: Functor<F>, monad: Monad<M>, block: suspend MonadScope<K2<T, F, M>>.() -> A): FreeT<F, M, A> =
            narrow(instance(functor, monad).fxReplay(block))

        // Transformer
        /** Lifts a single instruction into a program. */
        fun <F, M, A> liftF(functor: Functor<F>, monad: Monad<M>, fa: K1<F, A>): FreeT<F, M, A> =
            FreeT(functor, monad, monad.pure(FreeF.Free(functor.fmap({ a: A -> pure(functor, monad, a) }, fa))))
        /** Lifts an inner monadic value into a program. */
        fun <F, M, A> lift(functor: Functor<F>, monad: Monad<M>, v: K1<M, A>): FreeT<F, M, A> = instance(functor, monad).lift(v)

        private fun <F, M> instance(functor: Functor<F>, monad: Monad<M>) = FreeTMonadTrans.of(functor, monad)
    }

    // Monad
    /** Maps the result of this program. */
    infix fun <B> fmap(f: (A) -> B): FreeT<F, M, B> = instance().fmap(f, this)
    /** Applies the function produced by [f] to the result of this program. */
    infix fun <B> ap(f: FreeT<F, M, (A) -> B>): FreeT<F, M, B> = instance().ap(f, this)
    /** Sequences this program with [f]. */
    infix fun <B> bind(f: (A) -> FreeT<F, M, B>): FreeT<F, M, B> = instance().bind(f, this)

    /** Tears down the program in `M`, interpreting each instruction layer with [phi]. */
    fun iterT(phi: (K1<F, K1<M, A>>) -> K1<M, A>): K1<M, A> = monad.bind({ step: FreeF<F, A, FreeT<F, M, A>> ->
        when (step) {
            is FreeF.Pure -> monad.pure(step.value)
            is FreeF.Free -> phi(functor.fmap({ next: FreeT<F, M, A> -> next.iterT(phi) }, step.next))
        }
    }, run)

    /** Interprets every instruction into the inner monad with [nt]. */
    fun foldMap(nt: NaturalTransformation<F, M>): K1<M, A> = iterT { layer -> monad.join(nt(layer)) }

    /** Changes the inner monad with [nt]. */
    fun <N> hoistFreeT(monadN: Monad<N>, nt: NaturalTransformation<M, N>): FreeT<F, N, A> =
        object : FreeTMFunctor<F, M, N> { override val target = monadN }.hoist(nt, this)

    /** Changes the instruction functor with [nt]. */
    fun <G> transFreeT(functorG: Functor<G>, nt: NaturalTransformation<F, G>): FreeT<G, M, A> {
        val mapped = monad.fmap({ step: FreeF<F, A, FreeT<F, M, A>> -> step.mapNext(functor) { it.transFreeT(functorG, nt) }.transform(nt) }, run)
        return FreeT(functorG, monad, mapped)
    }

    private fun instance() = FreeTMonad.of(functor, monad)
}
