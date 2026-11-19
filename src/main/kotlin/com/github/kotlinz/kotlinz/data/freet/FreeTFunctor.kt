package com.github.kotlinz.kotlinz.data.freet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Functor instance for [FreeT]; requires `Functor<F>` and the inner `Monad<M>`. */
interface FreeTFunctor<F, M> : Functor<K2<FreeT.T, F, M>> {
    /** The instruction functor. */
    val functor: Functor<F>
    /** The inner monad. */
    val monad: Monad<M>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K2<FreeT.T, F, M>, A>): FreeT<F, M, B> {
        val step = { s: FreeF<F, A, FreeT<F, M, A>> ->
            when (s) {
                is FreeF.Pure -> FreeF.Pure(f(s.value))
                is FreeF.Free -> FreeF.Free<F, B, FreeT<F, M, B>>(functor.fmap({ next: FreeT<F, M, A> -> fmap(f, next) }, s.next))
            }
        }
        return FreeT(functor, monad, monad.fmap(step, FreeT.narrow(v).run))
    }
}
