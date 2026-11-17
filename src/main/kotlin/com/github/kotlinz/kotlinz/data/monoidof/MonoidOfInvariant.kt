package com.github.kotlinz.kotlinz.data.monoidof

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Invariant

interface MonoidOfInvariant: Invariant<MonoidOf.T> {
  override fun <A, B> imap(f: (A) -> B, g: (B) -> A, v: K1<MonoidOf.T, A>): MonoidOf<B> {
    val monoid = MonoidOf.narrow(v)
    return MonoidOf(f(monoid.empty)) { b1, b2 -> f(monoid.combine(g(b1), g(b2))) }
  }
}
