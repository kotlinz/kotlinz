package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Traversable
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Traversable instance for [Identity]. */
interface IdentityTraversable: Traversable<Identity.T>, IdentityFunctor, IdentityFoldable {
  override fun <G, A, B> traverse(applicative: Applicative<G>, f: (A) -> K1<G, B>, v: K1<Identity.T, A>): K1<G, K1<Identity.T, B>> =
    applicative.fmap({ b: B -> Identity(b) as K1<Identity.T, B> }, f(Identity.narrow(v).value))
}
