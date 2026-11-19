package com.github.kotlinz.kotlinz.data.select

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Select]. */
interface SelectFunctor<R>: Functor<K1<Select.T, R>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Select.T, R>, A>): Select<R, B> {
    val select = Select.narrow(v)
    return Select { k -> f(select.run { a -> k(f(a)) }) }
  }
}
