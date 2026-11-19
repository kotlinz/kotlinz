package com.github.kotlinz.kotlinz.data.env

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.ComonadEnv

/** ComonadEnv instance for [Env]. */
interface EnvComonadEnv<E>: ComonadEnv<K1<Env.T, E>, E>, EnvComonad<E> {
  override fun <A> ask(v: K1<K1<Env.T, E>, A>): E = Env.narrow(v).env
}
