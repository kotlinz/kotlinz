package com.github.kotlinz.kotlinz.data.store

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Comonad instance for [Store]. */
interface StoreComonad<S>: Comonad<K1<Store.T, S>>, StoreCopointed<S>, StoreFunctor<S> {
  override fun <A> duplicate(v: K1<K1<Store.T, S>, A>): Store<S, K1<K1<Store.T, S>, A>> {
    val store = Store.narrow(v)
    return Store({ s -> store.seek(s) }, store.pos)
  }

  override fun <A, B> extend(f: (K1<K1<Store.T, S>, A>) -> B, v: K1<K1<Store.T, S>, A>): Store<S, B> {
    return Store.narrow(fmap(f, duplicate(v)))
  }
}
