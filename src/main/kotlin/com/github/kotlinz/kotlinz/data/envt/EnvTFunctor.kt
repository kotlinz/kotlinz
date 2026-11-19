package com.github.kotlinz.kotlinz.data.envt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Functor instance for [EnvT]; requires the base [Comonad] via [comonad]. */
interface EnvTFunctor<E, W> : Functor<K2<EnvT.T, E, W>> {
    /** The base comonad. */
    val comonad: Comonad<W>

    override fun <A, B> fmap(f: (A) -> B, v: K1<K2<EnvT.T, E, W>, A>): EnvT<E, W, B> {
        val envT = EnvT.narrow(v)
        return EnvT(comonad, envT.env, comonad.fmap(f, envT.base))
    }
}
