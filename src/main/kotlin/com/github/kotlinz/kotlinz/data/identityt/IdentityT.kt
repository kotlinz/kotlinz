package com.github.kotlinz.kotlinz.data.identityt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.monad.MonadIO
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * The identity monad transformer: wraps `F<A>` without adding any effect.
 *
 * Useful as a neutral layer in generic transformer code and for testing transformer machinery.
 * Instances: [IdentityTFunctor], [IdentityTApplicative], [IdentityTMonad], [IdentityTMonadTrans],
 * [IdentityTMonadIO], [IdentityTMFunctor], plus [IdentityTMonadState], [IdentityTMonadReader],
 * [IdentityTMonadWriter] and [IdentityTMonadError] lifted from the base monad.
 *
 * ```kotlin
 * val maybe = object : MaybeMonad {}
 * val program = IdentityT.lift(maybe, Maybe.Just(1)) bind { x -> IdentityT.pure(maybe, x + 1) }
 * program.run // Just(2)
 * ```
 *
 * @property monad the underlying monad instance.
 * @property run the wrapped `F<A>`.
 */
class IdentityT<F, A>(val monad: Monad<F>, val run: K1<F, A>) : K2<IdentityT.T, F, A> {
    /** Higher-kinded witness tag for [IdentityT]. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [IdentityT]. */
        fun <F, A> narrow(v: K1<K1<T, F>, A>): IdentityT<F, A> = v as IdentityT<F, A>

        // Monad
        /** Wraps [v] using the underlying monad's `pure`. */
        fun <F, A> pure(monad: Monad<F>, v: A): IdentityT<F, A> = instance(monad).pure(v)
        /** Flattens a nested [IdentityT]. */
        fun <F, A> join(monad: Monad<F>, v: K1<K1<T, F>, K1<K1<T, F>, A>>): IdentityT<F, A> = instance(monad).join(v)
        /** Runs a monad comprehension block in [IdentityT]. */
        fun <F, A> fx(monad: Monad<F>, block: suspend MonadScope<K1<T, F>>.() -> A): IdentityT<F, A> = narrow(instance(monad).fx(block))

        // Transformer
        /** Lifts an `F<A>` into [IdentityT]. */
        fun <F, A> lift(monad: Monad<F>, v: K1<F, A>): IdentityT<F, A> = instance(monad).lift(v)
        /** Lifts an [IO] computation through the base monad's MonadIO instance. */
        fun <F, A> liftIO(monad: MonadIO<F>, v: IO<A>): IdentityT<F, A> = object: IdentityTMonadIO<F> { override val monad = monad }.liftIO(v)

        private fun <F> instance(monad: Monad<F>) = object: IdentityTMonadTrans<F> { override val monad = monad }
    }

    // Monad
    /** Maps the value. */
    infix fun <B> fmap(f: (A) -> B): IdentityT<F, B> = instance().fmap(f, this)
    /** Applies the function in [f] to the value. */
    infix fun <B> ap(f: IdentityT<F, (A) -> B>): IdentityT<F, B> = instance().ap(f, this)
    /** Sequences this computation with [f]. */
    infix fun <B> bind(f: (A) -> IdentityT<F, B>): IdentityT<F, B> = instance().bind(f, this)

    // MFunctor
    /** Swaps the base monad using [nt]; [target] is the monad of the new base. */
    fun <G> hoist(target: Monad<G>, nt: NaturalTransformation<F, G>): IdentityT<G, A> {
        return object: IdentityTMFunctor<F, G> { override val target = target }.hoist(nt, this)
    }

    private fun instance() = instance(monad)
}
