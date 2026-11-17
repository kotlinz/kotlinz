package com.github.kotlinz.kotlinz.data.env

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2

data class Env<E, A>(val env: E, val value: A): K2<Env.T, E, A> {
  class T

  companion object {
    fun <E, A> narrow(v: K1<K1<T, E>, A>): Env<E, A> = v as Env<E, A>
  }

  fun ask(): E = env
  fun <B> asks(f: (E) -> B): B = f(env)
  fun local(f: (E) -> E): Env<E, A> = Env(f(env), value)

  // Comonad
  infix fun <B> fmap(f: (A) -> B): Env<E, B> = comonad.fmap(f, this)
  fun extract(): A = comonad.extract(this)
  fun duplicate(): Env<E, K1<K1<T, E>, A>> = comonad.duplicate(this)
  infix fun <B> extend(f: (K1<K1<T, E>, A>) -> B): Env<E, B> = comonad.extend(f, this)
  private val comonad get() = object: EnvComonad<E> {}
}
