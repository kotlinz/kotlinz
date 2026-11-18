package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps

/** Derived applicative helpers (liftA..liftA3) for [Function1]. */
interface Function1ApplicativeOps<R>: ApplicativeOps<K2<Function1.T, Unit, R>>, Function1Monad<R> {
  override fun <A, B> liftA(f: (A) -> B): (K1<K2<Function1.T, Unit, R>, A>) -> Function1<R, B> {
    return { a -> ap(pure(f), a) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<K2<Function1.T, Unit, R>, A>, K1<K2<Function1.T, Unit, R>, B>) -> Function1<R, C> {
    return { a1, a2 -> ap(fmap(f.curried(), a1), a2) }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<K2<Function1.T, Unit, R>, A>, K1<K2<Function1.T, Unit, R>, B>, K1<K2<Function1.T, Unit, R>, C>) -> Function1<R, D> {
    return { a1, a2, a3 -> ap(ap(fmap(f.curried(), a1), a2), a3) }
  }
}
