package com.github.kotlinz.kotlinz.data.freet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [FreeT]. Stack-safety depends on the inner monad. */
interface FreeTMonad<F, M> : Monad<K2<FreeT.T, F, M>>, FreeTApplicative<F, M>, FreeTFunctor<F, M> {
    companion object {
        /** Creates the instance from `Functor<F>` and `Monad<M>`. */
        fun <F, M> of(functor: Functor<F>, monad: Monad<M>): FreeTMonad<F, M> {
            val (baseFunctor, baseMonad) = Pair(functor, monad)
            return object : FreeTMonad<F, M> {
                override val functor = baseFunctor
                override val monad = baseMonad
            }
        }
    }

    override fun <A> join(v: K1<K2<FreeT.T, F, M>, K1<K2<FreeT.T, F, M>, A>>): FreeT<F, M, A> = bind({ it }, v)

    override fun <A, B> bind(f: (A) -> K1<K2<FreeT.T, F, M>, B>, v: K1<K2<FreeT.T, F, M>, A>): FreeT<F, M, B> {
        val step = { s: FreeF<F, A, FreeT<F, M, A>> ->
            when (s) {
                is FreeF.Pure -> FreeT.narrow(f(s.value)).run
                is FreeF.Free -> monad.pure<FreeF<F, B, FreeT<F, M, B>>>(FreeF.Free(functor.fmap({ next: FreeT<F, M, A> -> bind(f, next) }, s.next)))
            }
        }
        return FreeT(functor, monad, monad.bind(step, FreeT.narrow(v).run))
    }
}
