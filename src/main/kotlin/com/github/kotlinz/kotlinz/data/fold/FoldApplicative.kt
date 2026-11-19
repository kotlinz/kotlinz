package com.github.kotlinz.kotlinz.data.fold

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [Fold] with a fixed input type [A]; both folds consume the inputs in one pass. */
interface FoldApplicative<A>: Applicative<K2<Fold.T, Unit, A>>, FoldFunctor<A> {
  override fun <B> pure(v: B): Fold<A, B> = Fold.of(Unit, { s, _: A -> s }) { v }

  override fun <B, C> ap(f: K1<K2<Fold.T, Unit, A>, (B) -> C>, v: K1<K2<Fold.T, Unit, A>, B>): Fold<A, C> {
    val ff = Fold.narrow(f)
    val fv = Fold.narrow(v)
    return Fold.of(
      Pair(ff.initial, fv.initial),
      { (sf, sv), a: A -> Pair(ff.step(sf, a), fv.step(sv, a)) },
      { (sf, sv) -> ff.done(sf)(fv.done(sv)) }
    )
  }
}
