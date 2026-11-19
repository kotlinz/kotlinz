package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Foldable1

/** Foldable1 instance for [Identity]: every reduction yields the wrapped value. */
interface IdentityFoldable1: Foldable1<Identity.T>, IdentityFoldable {
  override fun <A> reduceLeft(f: (A, A) -> A, v: K1<Identity.T, A>): A = Identity.narrow(v).value
  override fun <A> reduceRight(f: (A, A) -> A, v: K1<Identity.T, A>): A = Identity.narrow(v).value
}
