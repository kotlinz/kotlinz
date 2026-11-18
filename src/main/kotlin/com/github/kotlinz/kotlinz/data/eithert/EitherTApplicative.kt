package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [EitherT]; effects run sequentially and the first error short-circuits. */
interface EitherTApplicative<F, E> : Applicative<K2<EitherT.T, F, E>>, EitherTFunctor<F, E> {
    override fun <A> pure(v: A): EitherT<F, E, A> = EitherT(monad, monad.pure(Either.Right(v)))

    override fun <A, B> ap(f: K1<K2<EitherT.T, F, E>, (A) -> B>, v: K1<K2<EitherT.T, F, E>, A>): EitherT<F, E, B> {
        val run = monad.bind({ ef: Either<E, (A) -> B> ->
            when (ef) {
                is Either.Right -> fmap(ef.value, v).run
                is Either.Left -> monad.pure(Either.Left<E, B>(ef.value))
            }
        }, EitherT.narrow(f).run)
        return EitherT(monad, run)
    }
}
