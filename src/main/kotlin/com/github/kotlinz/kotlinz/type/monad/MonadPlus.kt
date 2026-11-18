package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Alternative

/**
 * A monad that is also an [Alternative], supporting failure and choice.
 *
 * Laws (in addition to the Alternative laws):
 * - Left zero: `bind(f, mzero()) == mzero()`
 * - Right zero: `bind({ mzero() }, m) == mzero()`
 *
 * @see com.github.kotlinz.kotlinz.type.applicative.Alternative
 */
interface MonadPlus<T>: Monad<T>, Alternative<T> {
  /** The failing computation; defaults to [empty]. */
  fun <A> mzero(): K1<T, A> = empty()
  /** Combines two computations; defaults to [orElse]. */
  fun <A> mplus(m1: K1<T, A>, m2: K1<T, A>): K1<T, A> = orElse(m1, m2)
}
