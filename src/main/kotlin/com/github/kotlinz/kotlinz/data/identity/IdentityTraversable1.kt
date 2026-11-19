package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable1
import com.github.kotlinz.kotlinz.type.applicative.Apply

/** Traversable1 instance for [Identity]. */
interface IdentityTraversable1: Traversable1<Identity.T>, IdentityTraversable, IdentityFoldable1 {
  override fun <G, A, B> traverse1(apply: Apply<G>, f: (A) -> K1<G, B>, v: K1<Identity.T, A>): K1<G, K1<Identity.T, B>> =
    apply.fmap({ b: B -> Identity(b) as K1<Identity.T, B> }, f(Identity.narrow(v).value))
}
