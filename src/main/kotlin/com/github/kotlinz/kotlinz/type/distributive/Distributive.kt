package com.github.kotlinz.kotlinz.type.distributive

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface Distributive<G>: Functor<G> {
  fun <F, A> distribute(functor: Functor<F>, fga: K1<F, K1<G, A>>): K1<G, K1<F, A>>

  fun <F, A, B> collect(functor: Functor<F>, f: (A) -> K1<G, B>, fa: K1<F, A>): K1<G, K1<F, B>> =
    distribute(functor, functor.fmap(f, fa))

  fun <F, A, B> cotraverse(functor: Functor<F>, f: (K1<F, A>) -> B, fga: K1<F, K1<G, A>>): K1<G, B> =
    fmap(f, distribute(functor, fga))
}
