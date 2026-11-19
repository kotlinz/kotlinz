package com.github.kotlinz.kotlinz.data.iort

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.data.ior.Ior
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.group.Semigroup
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.monad.MonadIO
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * Ior monad transformer: an effect `F` that yields `Ior<A, B>`.
 *
 * Adds warnings that accumulate (with the [Semigroup] whose class is [type]) next to a result, on top of
 * another monad. A pure [Ior.Left] stops the computation; [Ior.Both] keeps going and prepends its left value.
 * Instances: [IorTFunctor], [IorTApplicative], [IorTMonad], [IorTMonadError] (error type `A`), [IorTMonadTrans],
 * [IorTMonadIO] and [IorTMFunctor].
 *
 * ```kotlin
 * val identity = object : IdentityMonad {}
 * val type = StringMonoid::class.java
 * val program = IorT.both<Identity.T, StringMonoid, Int>(identity, type, StringMonoid("warn;"), 1) bind { x ->
 *     IorT.both(identity, type, StringMonoid("again;"), x + 1)
 * }
 * program.run // Identity(Both(StringMonoid("warn;again;"), 2))
 * ```
 *
 * @property monad the [Monad] instance for the underlying effect `F`.
 * @property type the Semigroup class used to combine left values.
 * @property run the wrapped effect producing an [Ior].
 */
class IorT<F, A: Semigroup<*, A>, B>(val monad: Monad<F>, val type: Class<A>, val run: K1<F, Ior<A, B>>) : K3<IorT.T, F, A, B> {
    /** Higher-kinded witness tag for [IorT]. */
    class T

    companion object {
        /** Downcasts a higher-kinded value back to [IorT]. */
        fun <F, A: Semigroup<*, A>, B> narrow(v: K1<K2<T, F, A>, B>): IorT<F, A, B> = v as IorT<F, A, B>

        // Monad
        /** Wraps [v] as a [Ior.Right]. */
        fun <F, A: Semigroup<*, A>, B> pure(monad: Monad<F>, type: Class<A>, v: B): IorT<F, A, B> = instance(monad, type).pure(v)
        /** Flattens a nested [IorT], accumulating left values. */
        fun <F, A: Semigroup<*, A>, B> join(monad: Monad<F>, type: Class<A>, v: K1<K2<T, F, A>, K1<K2<T, F, A>, B>>): IorT<F, A, B> = instance(monad, type).join(v)
        /** Runs a monad comprehension block in [IorT]. */
        fun <F, A: Semigroup<*, A>, B> fx(monad: Monad<F>, type: Class<A>, block: suspend MonadScope<K2<T, F, A>>.() -> B): IorT<F, A, B> = narrow(instance(monad, type).fx(block))

        // MonadError
        /** Creates a stopped computation holding only the left value [e]. */
        fun <F, A: Semigroup<*, A>, B> raiseError(monad: Monad<F>, type: Class<A>, e: A): IorT<F, A, B> = instance(monad, type).raiseError(e)

        // Constructors
        /** Lifts a plain [Ior]. */
        fun <F, A: Semigroup<*, A>, B> fromIor(monad: Monad<F>, type: Class<A>, v: Ior<A, B>): IorT<F, A, B> = IorT(monad, type, monad.pure(v))
        /** Creates an [Ior.Left]. */
        fun <F, A: Semigroup<*, A>, B> left(monad: Monad<F>, type: Class<A>, a: A): IorT<F, A, B> = fromIor(monad, type, Ior.Left(a))
        /** Creates an [Ior.Right]. */
        fun <F, A: Semigroup<*, A>, B> right(monad: Monad<F>, type: Class<A>, b: B): IorT<F, A, B> = fromIor(monad, type, Ior.Right(b))
        /** Creates an [Ior.Both]. */
        fun <F, A: Semigroup<*, A>, B> both(monad: Monad<F>, type: Class<A>, a: A, b: B): IorT<F, A, B> = fromIor(monad, type, Ior.Both(a, b))

        // Transformer
        /** Lifts an effect [v] into [IorT] as a [Ior.Right]. */
        fun <F, A: Semigroup<*, A>, B> lift(monad: Monad<F>, type: Class<A>, v: K1<F, B>): IorT<F, A, B> = instance(monad, type).lift(v)
        /** Lifts an [IO] computation through the base monad's MonadIO instance. */
        fun <F, A: Semigroup<*, A>, B> liftIO(monad: MonadIO<F>, type: Class<A>, v: IO<B>): IorT<F, A, B> {
            return object: IorTMonadIO<F, A> {
                override val monad = monad
                override val type = type
            }.liftIO(v)
        }

        private fun <F, A: Semigroup<*, A>> instance(monad: Monad<F>, type: Class<A>) = object: IorTMonadError<F, A>, IorTMonadTrans<F, A> {
            override val monad = monad
            override val type = type
        }
    }

    // Monad
    /** Maps the right value. */
    infix fun <C> fmap(f: (B) -> C): IorT<F, A, C> = instance().fmap(f, this)
    /** Applies the function inside [f], accumulating left values. */
    infix fun <C> ap(f: IorT<F, A, (B) -> C>): IorT<F, A, C> = instance().ap(f, this)
    /** Sequences [f], accumulating left values and stopping on a pure left. */
    infix fun <C> bind(f: (B) -> IorT<F, A, C>): IorT<F, A, C> = instance().bind(f, this)

    // MonadError
    /** Recovers from a pure left value by running [f] on it. */
    infix fun handleErrorWith(f: (A) -> IorT<F, A, B>): IorT<F, A, B> = instance().handleErrorWith(f, this)

    // MFunctor
    /** Swaps the base monad using [nt]; [target] is the monad of the new base. */
    fun <G> hoist(target: Monad<G>, nt: NaturalTransformation<F, G>): IorT<G, A, B> {
        val type = this.type
        return object: IorTMFunctor<F, G, A> {
            override val target = target
            override val type = type
        }.hoist(nt, this)
    }

    private fun instance() = instance(monad, type)
}
