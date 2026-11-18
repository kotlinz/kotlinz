package com.github.kotlinz.kotlinz.data.store

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.pointed.Copointed

/** Copointed instance for [Store]. */
interface StoreCopointed<S>: Copointed<K1<Store.T, S>> {
  override fun <A> extract(v: K1<K1<Store.T, S>, A>): A {
    val store = Store.narrow(v)
    return store.peek(store.pos)
  }
}
