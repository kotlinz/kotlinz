package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadPlus

interface MaybeMonadPlus: MonadPlus<Maybe.T>, MaybeMonad, MaybeAlternative {
  override fun <A> mzero(): Maybe<A> = empty()
  override fun <A> mplus(m1: K1<Maybe.T, A>, m2: K1<Maybe.T, A>): Maybe<A> = orElse(m1, m2)
}
