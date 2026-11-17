package com.github.kotlinz.kotlinz.data.compose

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps

interface ComposeApplicativeOps<F, G>: ApplicativeOps<K2<Compose.T, F, G>>, ComposeApplicative<F, G> {
  override fun <A, B> liftA(f: (A) -> B): (K1<K2<Compose.T, F, G>, A>) -> Compose<F, G, B> {
    return { a -> ap(pure(f), a) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<K2<Compose.T, F, G>, A>, K1<K2<Compose.T, F, G>, B>) -> Compose<F, G, C> {
    return { a1, a2 -> ap(fmap(f.curried(), a1), a2) }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<K2<Compose.T, F, G>, A>, K1<K2<Compose.T, F, G>, B>, K1<K2<Compose.T, F, G>, C>) -> Compose<F, G, D> {
    return { a1, a2, a3 -> ap(ap(fmap(f.curried(), a1), a2), a3) }
  }
}
