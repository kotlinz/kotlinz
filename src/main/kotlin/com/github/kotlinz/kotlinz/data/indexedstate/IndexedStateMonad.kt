package com.github.kotlinz.kotlinz.data.indexedstate

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [IndexedState] when the state type `S` does not change; delegates to [IndexedStateIxMonad]. */
interface IndexedStateMonad<S>: Monad<K2<IndexedState.T, S, S>> {
  companion object {
    /** Creates the instance for state type `S`. */
    fun <S> of(): IndexedStateMonad<S> = object: IndexedStateMonad<S> {}
  }

  override fun <A> pure(v: A): IndexedState<S, S, A> = IndexedState.pure(v)

  override fun <A, B> fmap(f: (A) -> B, v: K1<K2<IndexedState.T, S, S>, A>): IndexedState<S, S, B> = IndexedState.narrow(v) fmap f

  override fun <A, B> ap(f: K1<K2<IndexedState.T, S, S>, (A) -> B>, v: K1<K2<IndexedState.T, S, S>, A>): IndexedState<S, S, B> =
    IndexedState.narrow(v) ap IndexedState.narrow(f)

  override fun <A> join(v: K1<K2<IndexedState.T, S, S>, K1<K2<IndexedState.T, S, S>, A>>): IndexedState<S, S, A> = bind({ it }, v)

  override fun <A, B> bind(f: (A) -> K1<K2<IndexedState.T, S, S>, B>, v: K1<K2<IndexedState.T, S, S>, A>): IndexedState<S, S, B> =
    IndexedState.narrow(v) bind { a -> IndexedState.narrow(f(a)) }
}
