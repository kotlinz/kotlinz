package com.github.kotlinz.kotlinz.data.env

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Env]. */
interface EnvFunctor<E>: Functor<K1<Env.T, E>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Env.T, E>, A>): Env<E, B> {
    val env = Env.narrow(v)
    return Env(env.env, f(env.value))
  }
}
