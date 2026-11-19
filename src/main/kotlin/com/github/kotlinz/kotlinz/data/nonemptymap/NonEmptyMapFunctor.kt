package com.github.kotlinz.kotlinz.data.nonemptymap

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [NonEmptyMap], mapping the values and keeping the keys. */
interface NonEmptyMapFunctor<K>: Functor<K1<NonEmptyMap.T, K>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<NonEmptyMap.T, K>, A>): NonEmptyMap<K, B> =
    NonEmptyMap.wrap(NonEmptyMap.narrow(v).toMap().mapValuesTo(LinkedHashMap()) { (_, a) -> f(a) })
}
