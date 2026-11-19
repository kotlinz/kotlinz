package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.io.IO
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.monad.MonadIO
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

/**
 * Either monad transformer: an effect `F` that yields `Either<E, A>`.
 *
 * Use it to add short-circuiting typed errors on top of another monad (e.g. `IO`) without
 * manually threading `Either` through every `bind`. A [Either.Left] stops the computation.
 *
 * Instances: Functor, Applicative, Monad, MonadError (error type `E`) and MonadTrans, all requiring a [Monad] for `F`,
 * plus MFunctor.
 *
 * ```kotlin
 * val io = object : IOMonad {}
 * val program = EitherT.lift<IO.T, String, Int>(io, IO { 20 }) bind { n ->
 *   if (n > 10) EitherT.raiseError(io, "too big") else EitherT.pure(io, n * 2)
 * }
 * IO.narrow(program.run).unsafeRun() // Left("too big")
 * ```
 *
 * @property monad the [Monad] instance for the underlying effect `F`.
 * @property run the wrapped effect producing an [Either].
 */
class EitherT<F, E, A>(val monad: Monad<F>, val run: K1<F, Either<E, A>>) : K3<EitherT.T, F, E, A> {
    class T

    companion object {
        /** Downcasts a higher-kinded representation back to [EitherT]. */
        fun <F, E, A> narrow(v: K1<K2<T, F, E>, A>): EitherT<F, E, A> = v as EitherT<F, E, A>

        // Monad
        /** Lifts [v] into a successful [EitherT]. */
        fun <F, E, A> pure(monad: Monad<F>, v: A): EitherT<F, E, A> = instance<F, E>(monad).pure(v)
        /** Flattens a nested [EitherT]. */
        fun <F, E, A> join(monad: Monad<F>, v: K1<K2<T, F, E>, K1<K2<T, F, E>, A>>): EitherT<F, E, A> = instance<F, E>(monad).join(v)
        /** Runs a monad comprehension [block] in the [EitherT] monad. */
        fun <F, E, A> fx(monad: Monad<F>, block: suspend MonadScope<K2<T, F, E>>.() -> A): EitherT<F, E, A> = narrow(instance<F, E>(monad).fx(block))

        // MonadError
        /** Creates a failed [EitherT] holding the error [e]. */
        fun <F, E, A> raiseError(monad: Monad<F>, e: E): EitherT<F, E, A> = instance<F, E>(monad).raiseError(e)

        // Transformer
        /** Lifts an effect [v] into [EitherT] as a success. */
        fun <F, E, A> lift(monad: Monad<F>, v: K1<F, A>): EitherT<F, E, A> = instance<F, E>(monad).lift(v)
        /** Lifts an [IO] computation through the base monad's MonadIO instance. */
        fun <F, E, A> liftIO(monad: MonadIO<F>, v: IO<A>): EitherT<F, E, A> = object: EitherTMonadIO<F, E> { override val monad = monad }.liftIO(v)
        /** Lifts a pure [Either] into [EitherT]. */
        fun <F, E, A> fromEither(monad: Monad<F>, v: Either<E, A>): EitherT<F, E, A> = EitherT(monad, monad.pure(v))

        private fun <F, E> instance(monad: Monad<F>) = object: EitherTMonadError<F, E>, EitherTMonadTrans<F, E> { override val monad = monad }
    }

    // Monad
    /** Maps the success value with [f]. */
    infix fun <B> fmap(f: (A) -> B): EitherT<F, E, B> = instance().fmap(f, this)
    /** Applies the function inside [f] to the success value, short-circuiting on the first error. */
    infix fun <B> ap(f: EitherT<F, E, (A) -> B>): EitherT<F, E, B> = instance().ap(f, this)
    /** Chains a computation on the success value, short-circuiting on error. */
    infix fun <B> bind(f: (A) -> EitherT<F, E, B>): EitherT<F, E, B> = instance().bind(f, this)

    // MonadError
    /** Recovers from an error by running [f] on it. */
    infix fun handleErrorWith(f: (E) -> EitherT<F, E, A>): EitherT<F, E, A> = instance().handleErrorWith(f, this)
    /** Recovers from an error by mapping it to a success value. */
    infix fun handleError(f: (E) -> A): EitherT<F, E, A> = narrow(instance().handleError(f, this))

    // MFunctor
    /** Swaps the base monad using [nt]; [target] is the monad of the new base. */
    fun <G> hoist(target: Monad<G>, nt: NaturalTransformation<F, G>): EitherT<G, E, A> {
        return object: EitherTMFunctor<F, G, E> { override val target = target }.hoist(nt, this)
    }

    private fun instance() = object: EitherTMonadError<F, E> { override val monad = this@EitherT.monad }
}
