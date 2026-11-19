package com.github.kotlinz.kotlinz.type.pointed

import com.github.kotlinz.kotlinz.K3

/**
 * An indexed type constructor that can lift a plain value without changing the index.
 *
 * @see Pointed
 * @see com.github.kotlinz.kotlinz.type.applicative.IxApplicative
 */
interface IxPointed<T> {
  /** Wraps `v` in a context whose input and output indices are both `I`. */
  fun <I, A> ipure(v: A): K3<T, I, I, A>
}
