package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bitraversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Bitraversable instance for [Tuple2]; runs the effect of the first component before the second. */
interface Tuple2Bitraversable : Bitraversable<Tuple2.T>, Tuple2Bifunctor, Tuple2Bifoldable {
  override fun <G, A, B, C, D> bitraverse(applicative: Applicative<G>, f: (A) -> K1<G, C>, g: (B) -> K1<G, D>, v: K2<Tuple2.T, A, B>): K1<G, K2<Tuple2.T, C, D>> {
    val tuple = Tuple2.narrow(v)
    return applicative.ap(applicative.fmap({ c: C -> { d: D -> Tuple2(c, d) } }, f(tuple.first)), g(tuple.second))
  }
}
