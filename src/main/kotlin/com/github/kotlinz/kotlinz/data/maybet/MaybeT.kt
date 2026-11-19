package com.github.kotlinz.kotlinz.data.maybet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * Monad transformer that adds optionality ([Maybe]) on top of an arbitrary monad [F].
 *
 * Use it when a computation in some effect `F` (e.g. `IO`) may also produce no result, and you want
 * `bind` to short-circuit on [Maybe.None] without nesting `Maybe` handling inside `F`.
 *
 * Instances: Functor, Applicative, Monad, MonadTrans (each requires the underlying `Monad<F>`) and MFunctor.
 *
 * ```kotlin
 * val identity = object : IdentityMonad {}
 * val result = MaybeT.fx(identity) {
 *     val a = MaybeT.lift(identity, Identity(1)).bind()
 *     val b = MaybeT.pure(identity, 2).bind()
 *     a + b
 * }
 * result.run // Identity(Just(3))
 * MaybeT.none<Identity.T, Int>(identity).getOrElse(0) // Identity(0)
 * ```
 *
 * @property monad the underlying monad instance.
 * @property run the wrapped `F<Maybe<A>>`.
 */
class MaybeT<F, A>(val monad: Monad<F>, val run: K1<F, Maybe<A>>) : K2<MaybeT.T, F, A> {
    /** Witness type used to encode `MaybeT` as a higher-kinded `K2<MaybeT.T, F, A>`. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [MaybeT]. */
        fun <F, A> narrow(v: K1<K1<T, F>, A>): MaybeT<F, A> = v as MaybeT<F, A>

        // Monad
        /** Lifts a plain value into `MaybeT` as a present value. */
        fun <F, A> pure(monad: Monad<F>, v: A): MaybeT<F, A> = instance(monad).pure(v)
        /** Flattens a nested `MaybeT`. */
        fun <F, A> join(monad: Monad<F>, v: K1<K1<T, F>, K1<K1<T, F>, A>>): MaybeT<F, A> = instance(monad).join(v)
        /** Runs a monadic block where `bind()` short-circuits on [Maybe.None]. */
        fun <F, A> fx(monad: Monad<F>, block: suspend MonadScope<K1<T, F>>.() -> A): MaybeT<F, A> = narrow(instance(monad).fx(block))

        // Transformer
        /** Lifts an `F<A>` into `MaybeT`, treating its result as present. */
        fun <F, A> lift(monad: Monad<F>, v: K1<F, A>): MaybeT<F, A> = instance(monad).lift(v)
        /** Lifts a plain [Maybe] into `MaybeT`. */
        fun <F, A> fromMaybe(monad: Monad<F>, v: Maybe<A>): MaybeT<F, A> = MaybeT(monad, monad.pure(v))
        /** Creates an absent `MaybeT` value. */
        fun <F, A> none(monad: Monad<F>): MaybeT<F, A> = fromMaybe(monad, Maybe.None())

        private fun <F> instance(monad: Monad<F>) = object: MaybeTMonadTrans<F> { override val monad = monad }
    }

    // Monad
    /** Applies [f] to the value if present. */
    infix fun <B> fmap(f: (A) -> B): MaybeT<F, B> = instance().fmap(f, this)
    /** Applies the function in [f] to the value if both are present. */
    infix fun <B> ap(f: MaybeT<F, (A) -> B>): MaybeT<F, B> = instance().ap(f, this)
    /** Chains a computation, skipping it when this value is absent. */
    infix fun <B> bind(f: (A) -> MaybeT<F, B>): MaybeT<F, B> = instance().bind(f, this)

    /** Runs the computation in [F], substituting [v] when the result is absent. */
    fun getOrElse(v: A): K1<F, A> = monad.fmap({ m: Maybe<A> -> m.getOrElse(v) }, run)

    // MFunctor
    /** Swaps the base monad using [nt]; [target] is the monad of the new base. */
    fun <G> hoist(target: Monad<G>, nt: NaturalTransformation<F, G>): MaybeT<G, A> {
        return object: MaybeTMFunctor<F, G> { override val target = target }.hoist(nt, this)
    }

    private fun instance() = object: MaybeTMonad<F> { override val monad = this@MaybeT.monad }
}
