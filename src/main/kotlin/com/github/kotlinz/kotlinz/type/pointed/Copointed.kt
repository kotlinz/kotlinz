package com.github.kotlinz.kotlinz.type.pointed

import com.github.kotlinz.kotlinz.K1

/**
 * A type constructor whose context always holds a value that can be extracted.
 *
 * @see Pointed
 * @see com.github.kotlinz.kotlinz.type.monad.Comonad
 */
interface Copointed<T> {
  /** Returns the value held by `v`. */
  fun <A> extract(v: K1<T, A>): A
}
