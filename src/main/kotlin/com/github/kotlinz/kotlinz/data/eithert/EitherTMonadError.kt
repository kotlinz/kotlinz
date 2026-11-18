package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.monad.MonadError

/** MonadError instance for [EitherT] with `E` as the error type. */
interface EitherTMonadError<F, E> : MonadError<K2<EitherT.T, F, E>, E>, EitherTMonad<F, E> {
    override fun <A> raiseError(e: E): EitherT<F, E, A> = EitherT(monad, monad.pure(Either.Left(e)))

    override fun <A> handleErrorWith(f: (E) -> K1<K2<EitherT.T, F, E>, A>, v: K1<K2<EitherT.T, F, E>, A>): EitherT<F, E, A> {
        val run = monad.bind({ e: Either<E, A> ->
            when (e) {
                is Either.Left -> EitherT.narrow(f(e.value)).run
                is Either.Right -> monad.pure(e)
            }
        }, EitherT.narrow(v).run)
        return EitherT(monad, run)
    }
}
