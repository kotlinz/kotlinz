package com.github.kotlinz.kotlinz.data.fold

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Fold] with a fixed input type [A]; maps the result. */
interface FoldFunctor<A>: Functor<K2<Fold.T, Unit, A>> {
  override fun <B, C> fmap(f: (B) -> C, v: K1<K2<Fold.T, Unit, A>, B>): Fold<A, C> {
    val fold = Fold.narrow(v)
    return Fold.of(fold.initial, fold.step) { s -> f(fold.done(s)) }
  }
}
