package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [EitherT]. */
interface EitherTMonad<F, E> : Monad<K2<EitherT.T, F, E>>, EitherTApplicative<F, E>, EitherTFunctor<F, E> {
    override fun <A> join(v: K1<K2<EitherT.T, F, E>, K1<K2<EitherT.T, F, E>, A>>): EitherT<F, E, A> = bind({ it }, v)

    override fun <A, B> bind(f: (A) -> K1<K2<EitherT.T, F, E>, B>, v: K1<K2<EitherT.T, F, E>, A>): EitherT<F, E, B> {
        val run = monad.bind({ e: Either<E, A> ->
            when (e) {
                is Either.Right -> EitherT.narrow(f(e.value)).run
                is Either.Left -> monad.pure(Either.Left<E, B>(e.value))
            }
        }, EitherT.narrow(v).run)
        return EitherT(monad, run)
    }
}
