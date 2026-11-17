package com.github.kotlinz.kotlinz.data.equivalence

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Contravariant

interface EquivalenceContravariant: Contravariant<Equivalence.T> {
  override fun <A, B> contramap(f: (B) -> A, v: K1<Equivalence.T, A>): Equivalence<B> =
    Equivalence { b1, b2 -> Equivalence.narrow(v).equiv(f(b1), f(b2)) }
}
