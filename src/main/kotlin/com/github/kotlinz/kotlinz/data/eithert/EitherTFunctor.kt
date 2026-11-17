package com.github.kotlinz.kotlinz.data.eithert

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Monad

interface EitherTFunctor<F, E> : Functor<K2<EitherT.T, F, E>> {
    val monad: Monad<F>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K2<EitherT.T, F, E>, A>): EitherT<F, E, B> {
        return EitherT(monad, monad.fmap({ e: Either<E, A> -> e fmap f }, EitherT.narrow(v).run))
    }
}
