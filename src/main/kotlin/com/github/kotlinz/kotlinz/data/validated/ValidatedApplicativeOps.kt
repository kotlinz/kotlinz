package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps
import com.github.kotlinz.kotlinz.type.group.Semigroup

/** Applicative lifting helpers for [Validated]. */
interface ValidatedApplicativeOps<E: Semigroup<*, E>>: ApplicativeOps<K1<Validated.T, E>>, ValidatedApplicative<E> {
  override fun <A, B> liftA(f: (A) -> B): (K1<K1<Validated.T, E>, A>) -> Validated<E, B> {
    return { a -> ap(pure(f), a) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<K1<Validated.T, E>, A>, K1<K1<Validated.T, E>, B>) -> Validated<E, C> {
    return { a1, a2 -> ap(fmap(f.curried(), a1), a2) }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<K1<Validated.T, E>, A>, K1<K1<Validated.T, E>, B>, K1<K1<Validated.T, E>, C>) -> Validated<E, D> {
    return { a1, a2, a3 -> ap(ap(fmap(f.curried(), a1), a2), a3) }
  }
}
