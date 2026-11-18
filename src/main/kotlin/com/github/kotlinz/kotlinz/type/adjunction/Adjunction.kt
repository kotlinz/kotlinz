package com.github.kotlinz.kotlinz.type.adjunction

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/**
 * An adjunction `F -| G` between two functors: maps `F<A> -> B` correspond one-to-one with maps
 * `A -> G<B>`. Every adjunction yields a monad `G . F` and a comonad `F . G`.
 *
 * Laws:
 * - `rightAdjunct(leftAdjunct(f)) == f`
 * - `leftAdjunct(rightAdjunct(g)) == g`
 * - Triangle identities: `counit(left.fmap(::unit, fa)) == fa` and `right.fmap(::counit, unit(ga)) == ga`
 *
 * @param F the left adjoint.
 * @param G the right adjoint.
 */
interface Adjunction<F, G> {
  /** The left adjoint functor. */
  val left: Functor<F>
  /** The right adjoint functor. */
  val right: Functor<G>

  /** The unit `A -> G<F<A>>` (the `pure` of the induced monad). */
  fun <A> unit(a: A): K1<G, K1<F, A>>
  /** The counit `F<G<A>> -> A` (the `extract` of the induced comonad). */
  fun <A> counit(fga: K1<F, K1<G, A>>): A

  /** Transposes a map out of `F` into a map into `G`. */
  fun <A, B> leftAdjunct(f: (K1<F, A>) -> B): (A) -> K1<G, B> = { a -> right.fmap(f, unit(a)) }
  /** Transposes a map into `G` into a map out of `F`. */
  fun <A, B> rightAdjunct(f: (A) -> K1<G, B>): (K1<F, A>) -> B = { fa -> counit(left.fmap(f, fa)) }

  /** The `join` of the induced monad `G . F`. */
  fun <A> joinComposed(v: K1<G, K1<F, K1<G, K1<F, A>>>>): K1<G, K1<F, A>> =
    right.fmap({ fgfa: K1<F, K1<G, K1<F, A>>> -> counit(fgfa) }, v)

  /** The `duplicate` of the induced comonad `F . G`. */
  fun <A> duplicateComposed(v: K1<F, K1<G, A>>): K1<F, K1<G, K1<F, K1<G, A>>>> =
    left.fmap({ ga: K1<G, A> -> unit(ga) }, v)
}
