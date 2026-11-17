package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps
import com.github.kotlinz.kotlinz.type.group.Semigroup

interface IorApplicativeOps<S: Semigroup<*, S>>: ApplicativeOps<K1<Ior.T, S>>, IorMonad<S> {
  override fun <A, B> liftA(f: (A) -> B): (K1<K1<Ior.T, S>, A>) -> Ior<S, B> {
    return { a -> ap(pure(f), a) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<K1<Ior.T, S>, A>, K1<K1<Ior.T, S>, B>) -> Ior<S, C> {
    return { a1, a2 -> ap(fmap(f.curried(), a1), a2) }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<K1<Ior.T, S>, A>, K1<K1<Ior.T, S>, B>, K1<K1<Ior.T, S>, C>) -> Ior<S, D> {
    return { a1, a2, a3 -> ap(ap(fmap(f.curried(), a1), a2), a3) }
  }
}
