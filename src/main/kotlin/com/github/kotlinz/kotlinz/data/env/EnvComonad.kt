package com.github.kotlinz.kotlinz.data.env

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Comonad

interface EnvComonad<E>: Comonad<K1<Env.T, E>>, EnvCopointed<E>, EnvFunctor<E> {
  override fun <A> duplicate(v: K1<K1<Env.T, E>, A>): Env<E, K1<K1<Env.T, E>, A>> {
    val env = Env.narrow(v)
    return Env(env.env, env)
  }

  override fun <A, B> extend(f: (K1<K1<Env.T, E>, A>) -> B, v: K1<K1<Env.T, E>, A>): Env<E, B> {
    return Env.narrow(fmap(f, duplicate(v)))
  }
}
