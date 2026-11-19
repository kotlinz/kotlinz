package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bitraversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Bitraversable instance for [Ior]; [Ior.Both] runs the left effect before the right one. */
interface IorBitraversable : Bitraversable<Ior.T>, IorBifunctor, IorBifoldable {
  override fun <G, A, B, C, D> bitraverse(applicative: Applicative<G>, f: (A) -> K1<G, C>, g: (B) -> K1<G, D>, v: K2<Ior.T, A, B>): K1<G, K2<Ior.T, C, D>> {
    return when (val ior = Ior.narrow(v)) {
      is Ior.Left -> applicative.fmap({ c: C -> Ior.left<C, D>(c) }, f(ior.value))
      is Ior.Right -> applicative.fmap({ d: D -> Ior.right<C, D>(d) }, g(ior.value))
      is Ior.Both -> applicative.ap(applicative.fmap({ c: C -> { d: D -> Ior.both(c, d) } }, f(ior.left)), g(ior.right))
    }
  }
}
