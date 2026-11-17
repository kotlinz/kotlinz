package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.curried
import com.github.kotlinz.kotlinz.type.applicative.ApplicativeOps

interface Function0ApplicativeOps: ApplicativeOps<Function0.T>, Function0Monad {
  override fun <A, B> liftA(f: (A) -> B): (K1<Function0.T, A>) -> Function0<B> {
    return { a -> ap(pure(f), a) }
  }

  override fun <A, B, C> liftA2(f: (A, B) -> C): (K1<Function0.T, A>, K1<Function0.T, B>) -> Function0<C> {
    return { a1, a2 -> ap(fmap(f.curried(), a1), a2) }
  }

  override fun <A, B, C, D> liftA3(f: (A, B, C) -> D): (K1<Function0.T, A>, K1<Function0.T, B>, K1<Function0.T, C>) -> Function0<D> {
    return { a1, a2, a3 -> ap(ap(fmap(f.curried(), a1), a2), a3) }
  }
}
