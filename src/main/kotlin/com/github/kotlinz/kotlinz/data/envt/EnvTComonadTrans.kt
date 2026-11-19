package com.github.kotlinz.kotlinz.data.envt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.ComonadTrans

/** ComonadTrans instance for [EnvT]: lowering drops the environment. */
interface EnvTComonadTrans<E, W> : ComonadTrans<W, K2<EnvT.T, E, W>>, EnvTComonad<E, W> {
    override fun <A> lower(v: K1<K2<EnvT.T, E, W>, A>): K1<W, A> = EnvT.narrow(v).base
}
