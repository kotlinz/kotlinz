package com.github.kotlinz.kotlinz.type.align

import com.github.kotlinz.kotlinz.K1

/**
 * A [Semialign] with an empty structure [nil] that is the identity of [align].
 *
 * Laws:
 * - Right identity: `align(x, nil()) == fmap({ Ior.left(it) }, x)`
 * - Left identity: `align(nil(), x) == fmap({ Ior.right(it) }, x)`
 *
 * @see Semialign
 */
interface Align<T>: Semialign<T> {
  /** The empty structure. */
  fun <A> nil(): K1<T, A>
}
