package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.IxFunctor
import com.github.kotlinz.kotlinz.type.pointed.IxPointed

/**
 * An indexed applicative: sequencing `I -> J` with `J -> K` yields `I -> K`.
 *
 * Laws are those of [Applicative] with [ipure] and [iap], where the indices line up.
 *
 * @see Applicative
 * @see com.github.kotlinz.kotlinz.type.monad.IxMonad
 */
interface IxApplicative<T>: IxFunctor<T>, IxPointed<T> {
  /** Applies the function produced by `f` to the value produced by `v`, running `f` first. */
  fun <I, J, K, A, B> iap(f: K3<T, I, J, (A) -> B>, v: K3<T, J, K, A>): K3<T, I, K, B>
}
