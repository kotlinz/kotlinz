package com.github.kotlinz.kotlinz.data.store

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

/**
 * The store comonad: a lookup function `S -> A` together with a current position `S`.
 *
 * Use it for context-dependent computations such as cellular automata or image filters,
 * where each result depends on neighbouring positions. Has Functor, Copointed and Comonad instances.
 *
 * ```kotlin
 * val store = Store({ s: Int -> s * 10 }, 2)
 * store.extract()             // 20
 * store.seek(4).extract()     // 40
 * val sum = store extend { w -> Store.narrow(w).let { it.peeks { s -> s - 1 } + it.peeks { s -> s + 1 } } }
 * sum.extract()               // 40
 * ```
 *
 * @property peek looks up the value at any position.
 * @property pos the current position.
 */
class Store<S, A>(val peek: (S) -> A, val pos: S): K2<Store.T, S, A> {
  class T

  companion object {
    /** Downcasts a higher-kinded value to its concrete type. */
    fun <S, A> narrow(v: K1<K1<T, S>, A>): Store<S, A> = v as Store<S, A>
  }

  /** Moves to the absolute position `s`. */
  fun seek(s: S): Store<S, A> = Store(peek, s)
  /** Moves to a position relative to the current one. */
  fun seeks(f: (S) -> S): Store<S, A> = Store(peek, f(pos))
  /** Looks up the value at a position relative to the current one. */
  fun peeks(f: (S) -> S): A = peek(f(pos))
  /** Looks up values at every position produced by `f`, inside functor `F`. */
  fun <F> experiment(functor: Functor<F>, f: (S) -> K1<F, S>): K1<F, A> = functor.fmap(peek, f(pos))

  // Comonad
  /** Maps every value of the store. */
  infix fun <B> fmap(f: (A) -> B): Store<S, B> = comonad.fmap(f, this)
  /** Returns the value at the current position. */
  fun extract(): A = comonad.extract(this)
  /** Returns a store of stores, each focused on its own position. */
  fun duplicate(): Store<S, K1<K1<T, S>, A>> = comonad.duplicate(this)
  /** Computes a new store by applying `f` to the store focused at every position. */
  infix fun <B> extend(f: (K1<K1<T, S>, A>) -> B): Store<S, B> = comonad.extend(f, this)
  private val comonad = object: StoreComonad<S> {}
}
