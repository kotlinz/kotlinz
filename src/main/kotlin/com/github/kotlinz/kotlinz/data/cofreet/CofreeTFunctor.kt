package com.github.kotlinz.kotlinz.data.cofreet

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Functor instance for [CofreeT]; requires `Functor<F>` and the base `Comonad<W>`. Tails are mapped lazily. */
interface CofreeTFunctor<F, W> : Functor<K2<CofreeT.T, F, W>> {
    /** The branching functor. */
    val functor: Functor<F>
    /** The base comonad. */
    val comonad: Comonad<W>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K2<CofreeT.T, F, W>, A>): CofreeT<F, W, B> {
        val layer = { l: CofreeF<F, A, CofreeT<F, W, A>> -> l.bimap(functor, f) { next -> fmap(f, next) } }
        return CofreeT(functor, comonad, comonad.fmap(layer, CofreeT.narrow(v).run))
    }
}
