package com.github.kotlinz.kotlinz.data.freet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.monad.MonadTrans

/** MonadTrans instance for [FreeT]: lifted computations become finished layers without instructions. */
interface FreeTMonadTrans<F, M> : MonadTrans<M, K2<FreeT.T, F, M>>, FreeTMonad<F, M> {
    companion object {
        /** Creates the instance from `Functor<F>` and `Monad<M>`. */
        fun <F, M> of(functor: Functor<F>, monad: Monad<M>): FreeTMonadTrans<F, M> {
            val (baseFunctor, baseMonad) = Pair(functor, monad)
            return object : FreeTMonadTrans<F, M> {
                override val functor = baseFunctor
                override val monad = baseMonad
            }
        }
    }

    override fun <A> lift(v: K1<M, A>): FreeT<F, M, A> = FreeT(functor, monad, monad.fmap({ a: A -> FreeF.Pure(a) }, v))
}
