package com.github.kotlinz.kotlinz.data.store

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Store]. */
interface StoreFunctor<S>: Functor<K1<Store.T, S>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Store.T, S>, A>): Store<S, B> {
    val store = Store.narrow(v)
    return Store({ s -> f(store.peek(s)) }, store.pos)
  }
}
