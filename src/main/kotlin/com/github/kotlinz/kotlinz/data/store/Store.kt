package com.github.kotlinz.kotlinz.data.store

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

class Store<S, A>(val peek: (S) -> A, val pos: S): K2<Store.T, S, A> {
  class T

  companion object {
    fun <S, A> narrow(v: K1<K1<T, S>, A>): Store<S, A> = v as Store<S, A>
  }

  fun seek(s: S): Store<S, A> = Store(peek, s)
  fun seeks(f: (S) -> S): Store<S, A> = Store(peek, f(pos))
  fun peeks(f: (S) -> S): A = peek(f(pos))
  fun <F> experiment(functor: Functor<F>, f: (S) -> K1<F, S>): K1<F, A> = functor.fmap(peek, f(pos))

  // Comonad
  infix fun <B> fmap(f: (A) -> B): Store<S, B> = comonad.fmap(f, this)
  fun extract(): A = comonad.extract(this)
  fun duplicate(): Store<S, K1<K1<T, S>, A>> = comonad.duplicate(this)
  infix fun <B> extend(f: (K1<K1<T, S>, A>) -> B): Store<S, B> = comonad.extend(f, this)
  private val comonad = object: StoreComonad<S> {}
}
