package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable

interface IdentityFoldable: Foldable<Identity.T> {
  override fun <A, B> foldl(initial: B, f: (B, A) -> B, v: K1<Identity.T, A>): B = f(initial, Identity.narrow(v).value)
  override fun <A, B> foldr(initial: B, f: (A, B) -> B, v: K1<Identity.T, A>): B = f(Identity.narrow(v).value, initial)
}
