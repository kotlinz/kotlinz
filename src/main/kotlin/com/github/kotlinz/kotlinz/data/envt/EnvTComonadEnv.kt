package com.github.kotlinz.kotlinz.data.envt

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.ComonadEnv

/** ComonadEnv instance for [EnvT]. */
interface EnvTComonadEnv<E, W> : ComonadEnv<K2<EnvT.T, E, W>, E>, EnvTComonad<E, W> {
    override fun <A> ask(v: K1<K2<EnvT.T, E, W>, A>): E = EnvT.narrow(v).env
}
