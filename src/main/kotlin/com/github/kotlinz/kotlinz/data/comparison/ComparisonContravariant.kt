package com.github.kotlinz.kotlinz.data.comparison

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Contravariant

interface ComparisonContravariant: Contravariant<Comparison.T> {
  override fun <A, B> contramap(f: (B) -> A, v: K1<Comparison.T, A>): Comparison<B> =
    Comparison { b1, b2 -> Comparison.narrow(v).compare(f(b1), f(b2)) }
}
