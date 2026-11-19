package com.github.kotlinz.kotlinz.data.envt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Comonad instance for [EnvT]; extracts from and extends the base comonad, keeping the environment. */
interface EnvTComonad<E, W> : Comonad<K2<EnvT.T, E, W>>, EnvTFunctor<E, W> {
    override fun <A> extract(v: K1<K2<EnvT.T, E, W>, A>): A = comonad.extract(EnvT.narrow(v).base)

    override fun <A> duplicate(v: K1<K2<EnvT.T, E, W>, A>): EnvT<E, W, K1<K2<EnvT.T, E, W>, A>> = extend({ it }, v)

    override fun <A, B> extend(f: (K1<K2<EnvT.T, E, W>, A>) -> B, v: K1<K2<EnvT.T, E, W>, A>): EnvT<E, W, B> {
        val envT = EnvT.narrow(v)
        return EnvT(comonad, envT.env, comonad.extend({ w: K1<W, A> -> f(EnvT(comonad, envT.env, w)) }, envT.base))
    }
}
