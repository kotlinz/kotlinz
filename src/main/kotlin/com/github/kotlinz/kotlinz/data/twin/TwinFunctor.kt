package com.github.kotlinz.kotlinz.data.twin

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Twin]. */
interface TwinFunctor: Functor<Twin.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Twin.T, A>): Twin<B> {
    val twin = Twin.narrow(v)
    return Twin(f(twin.first), f(twin.second))
  }
}
