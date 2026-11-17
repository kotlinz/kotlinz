package com.github.kotlinz.kotlinz.type.adjunction

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface Adjunction<F, G> {
  val left: Functor<F>
  val right: Functor<G>

  fun <A> unit(a: A): K1<G, K1<F, A>>
  fun <A> counit(fga: K1<F, K1<G, A>>): A

  fun <A, B> leftAdjunct(f: (K1<F, A>) -> B): (A) -> K1<G, B> = { a -> right.fmap(f, unit(a)) }
  fun <A, B> rightAdjunct(f: (A) -> K1<G, B>): (K1<F, A>) -> B = { fa -> counit(left.fmap(f, fa)) }

  fun <A> joinComposed(v: K1<G, K1<F, K1<G, K1<F, A>>>>): K1<G, K1<F, A>> =
    right.fmap({ fgfa: K1<F, K1<G, K1<F, A>>> -> counit(fgfa) }, v)

  fun <A> duplicateComposed(v: K1<F, K1<G, A>>): K1<F, K1<G, K1<F, K1<G, A>>>> =
    left.fmap({ ga: K1<G, A> -> unit(ga) }, v)
}
