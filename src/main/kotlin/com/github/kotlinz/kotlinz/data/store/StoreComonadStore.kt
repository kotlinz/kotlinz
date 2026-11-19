package com.github.kotlinz.kotlinz.data.store

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.ComonadStore

/** ComonadStore instance for [Store]. */
interface StoreComonadStore<S>: ComonadStore<K1<Store.T, S>, S>, StoreComonad<S> {
  override fun <A> pos(v: K1<K1<Store.T, S>, A>): S = Store.narrow(v).pos
  override fun <A> peek(s: S, v: K1<K1<Store.T, S>, A>): A = Store.narrow(v).peek(s)
  override fun <A> seek(s: S, v: K1<K1<Store.T, S>, A>): Store<S, A> = Store.narrow(v).seek(s)
}
