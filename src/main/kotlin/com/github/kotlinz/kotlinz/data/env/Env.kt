package com.github.kotlinz.kotlinz.data.env

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2

/**
 * A value [value] paired with a read-only environment [env] (the environment comonad, a.k.a. coreader).
 *
 * Use it when a context-dependent computation should see a shared environment alongside the focused value;
 * it is the comonadic dual of `Reader` and is left adjoint to it (see [EnvReaderAdjunction]).
 *
 * Instances: Functor, Copointed and Comonad.
 *
 * ```kotlin
 * val env = Env(10, "value")
 * env.local { it + 1 }                                                  // Env(11, "value")
 * env extend { w -> Env.narrow(w).let { "${it.value}:${it.ask()}" } }   // Env(10, "value:10")
 * ```
 */
data class Env<E, A>(val env: E, val value: A): K2<Env.T, E, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded representation back to [Env]. */
    fun <E, A> narrow(v: K1<K1<T, E>, A>): Env<E, A> = v as Env<E, A>
  }

  /** Returns the environment. */
  fun ask(): E = env
  /** Returns the environment projected through [f]. */
  fun <B> asks(f: (E) -> B): B = f(env)
  /** Returns a copy with the environment modified by [f]. */
  fun local(f: (E) -> E): Env<E, A> = Env(f(env), value)

  // Comonad
  /** Maps the focused value with [f], keeping the environment. */
  infix fun <B> fmap(f: (A) -> B): Env<E, B> = comonad.fmap(f, this)
  /** Returns the focused value. */
  fun extract(): A = comonad.extract(this)
  /** Wraps this [Env] as the value of a new [Env] with the same environment. */
  fun duplicate(): Env<E, K1<K1<T, E>, A>> = comonad.duplicate(this)
  /** Computes a new value from the whole [Env] with [f], keeping the environment. */
  infix fun <B> extend(f: (K1<K1<T, E>, A>) -> B): Env<E, B> = comonad.extend(f, this)
  private val comonad get() = object: EnvComonad<E> {}
}
