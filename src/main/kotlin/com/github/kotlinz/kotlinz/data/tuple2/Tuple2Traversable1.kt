package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable1
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.applicative.Apply

/** Traversable1 (and Traversable) instance for [Tuple2], traversing its second component and keeping the first. */
interface Tuple2Traversable1<S>: Traversable1<K1<Tuple2.T, S>>, Tuple2Functor<S>, Tuple2Foldable1<S> {
  override fun <G, A, B> traverse1(apply: Apply<G>, f: (A) -> K1<G, B>, v: K1<K1<Tuple2.T, S>, A>): K1<G, K1<K1<Tuple2.T, S>, B>> {
    val tuple = Tuple2.narrow(v)
    return apply.fmap({ b: B -> Tuple2(tuple.first, b) as K1<K1<Tuple2.T, S>, B> }, f(tuple.second))
  }

  override fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<K1<Tuple2.T, S>, A>): K1<G, K1<K1<Tuple2.T, S>, B>> =
    traverse1(applicative, f, v)
}
