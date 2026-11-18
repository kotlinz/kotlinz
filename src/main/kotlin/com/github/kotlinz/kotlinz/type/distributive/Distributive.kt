package com.github.kotlinz.kotlinz.type.distributive

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/**
 * The dual of [com.github.kotlinz.kotlinz.type.Traversable]: a functor that can be pulled out of any
 * other functor. Every representable functor is distributive.
 *
 * Laws:
 * - `distribute(G, distribute(F, v)) == v` (for `F` and `G` both distributive)
 * - `collect(F, f, fa) == distribute(F, F.fmap(f, fa))`
 *
 * @see com.github.kotlinz.kotlinz.type.representable.Representable
 */
interface Distributive<G>: Functor<G> {
  /** Swaps the outer functor `F` with this functor `G`. */
  fun <F, A> distribute(functor: Functor<F>, fga: K1<F, K1<G, A>>): K1<G, K1<F, A>>

  /** Maps with `f` and then distributes. */
  fun <F, A, B> collect(functor: Functor<F>, f: (A) -> K1<G, B>, fa: K1<F, A>): K1<G, K1<F, B>> =
    distribute(functor, functor.fmap(f, fa))

  /** Distributes and then reduces each `F` layer with `f`. */
  fun <F, A, B> cotraverse(functor: Functor<F>, f: (K1<F, A>) -> B, fga: K1<F, K1<G, A>>): K1<G, B> =
    fmap(f, distribute(functor, fga))
}
