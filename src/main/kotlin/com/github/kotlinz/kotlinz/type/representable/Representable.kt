package com.github.kotlinz.kotlinz.type.representable

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/**
 * A functor isomorphic to functions from a fixed representation type `Rep`, i.e. `F<A> ~ (Rep) -> A`.
 *
 * Laws:
 * - `index(tabulate(f)) == f`
 * - `tabulate(index(fa)) == fa`
 *
 * @see com.github.kotlinz.kotlinz.type.distributive.Distributive
 */
interface Representable<F, Rep>: Functor<F> {
  /** Builds a structure by sampling `f` at every position. */
  fun <A> tabulate(f: (Rep) -> A): K1<F, A>
  /** Returns the function that looks up a position in `fa`. */
  fun <A> index(fa: K1<F, A>): (Rep) -> A
}
