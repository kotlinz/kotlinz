package com.github.kotlinz.kotlinz.data.freet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [FreeT]; runs the function program first. */
interface FreeTApplicative<F, M> : Applicative<K2<FreeT.T, F, M>>, FreeTFunctor<F, M> {
    override fun <A> pure(v: A): FreeT<F, M, A> = FreeT(functor, monad, monad.pure(FreeF.Pure(v)))

    override fun <A, B> ap(f: K1<K2<FreeT.T, F, M>, (A) -> B>, v: K1<K2<FreeT.T, F, M>, A>): FreeT<F, M, B> =
        FreeTMonad.of(functor, monad).bind({ g: (A) -> B -> fmap(g, v) }, f)
}
