package com.github.kotlinz.kotlinz.data.semigroupof

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Invariant

interface SemigroupOfInvariant: Invariant<SemigroupOf.T> {
  override fun <A, B> imap(f: (A) -> B, g: (B) -> A, v: K1<SemigroupOf.T, A>): SemigroupOf<B> =
    SemigroupOf { b1, b2 -> f(SemigroupOf.narrow(v).combine(g(b1), g(b2))) }
}
