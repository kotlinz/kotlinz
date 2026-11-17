package com.github.kotlinz.kotlinz.data.predicate

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Contravariant

interface PredicateContravariant: Contravariant<Predicate.T> {
  override fun <A, B> contramap(f: (B) -> A, v: K1<Predicate.T, A>): Predicate<B> =
    Predicate { b -> Predicate.narrow(v).test(f(b)) }
}
