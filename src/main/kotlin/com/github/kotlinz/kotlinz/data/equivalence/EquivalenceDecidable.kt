package com.github.kotlinz.kotlinz.data.equivalence

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.divisible.Decidable

interface EquivalenceDecidable: Decidable<Equivalence.T>, EquivalenceContravariant {
  override fun <A, B, C> divide(f: (A) -> Pair<B, C>, fb: K1<Equivalence.T, B>, fc: K1<Equivalence.T, C>): Equivalence<A> =
    Equivalence { a1, a2 ->
      val (b1, c1) = f(a1)
      val (b2, c2) = f(a2)
      Equivalence.narrow(fb).equiv(b1, b2) && Equivalence.narrow(fc).equiv(c1, c2)
    }

  override fun <A> conquer(): Equivalence<A> = Equivalence { _, _ -> true }

  override fun <A, B, C> choose(f: (A) -> Either<B, C>, fb: K1<Equivalence.T, B>, fc: K1<Equivalence.T, C>): Equivalence<A> =
    Equivalence { a1, a2 -> equivEither(f(a1), f(a2), Equivalence.narrow(fb), Equivalence.narrow(fc)) }

  override fun <A> lose(f: (A) -> Nothing): Equivalence<A> = Equivalence { a, _ -> f(a) }
}

private fun <B, C> equivEither(e1: Either<B, C>, e2: Either<B, C>, fb: Equivalence<B>, fc: Equivalence<C>): Boolean = when {
  e1 is Either.Left && e2 is Either.Left -> fb.equiv(e1.value, e2.value)
  e1 is Either.Right && e2 is Either.Right -> fc.equiv(e1.value, e2.value)
  else -> false
}
