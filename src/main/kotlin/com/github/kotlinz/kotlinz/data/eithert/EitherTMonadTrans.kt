package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [EitherT]: lifted computations always succeed. */
interface EitherTMonadTrans<F, E> : MonadTrans<F, K2<EitherT.T, F, E>>, EitherTMonad<F, E> {
    override fun <A> lift(v: K1<F, A>): EitherT<F, E, A> = EitherT(monad, monad.fmap({ a -> Either.Right<E, A>(a) }, v))
}
