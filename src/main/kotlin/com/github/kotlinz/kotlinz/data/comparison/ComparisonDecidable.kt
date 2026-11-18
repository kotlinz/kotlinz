package com.github.kotlinz.kotlinz.data.comparison

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.divisible.Decidable

/**
 * Divisible and Decidable instance for [Comparison].
 *
 * divide compares lexicographically; choose orders every Left before every Right.
 */
interface ComparisonDecidable: Decidable<Comparison.T>, ComparisonContravariant {
  override fun <A, B, C> divide(f: (A) -> Pair<B, C>, fb: K1<Comparison.T, B>, fc: K1<Comparison.T, C>): Comparison<A> =
    Comparison { a1, a2 ->
      val (b1, c1) = f(a1)
      val (b2, c2) = f(a2)
      Comparison.narrow(fb).compare(b1, b2).takeIf { it != 0 } ?: Comparison.narrow(fc).compare(c1, c2)
    }

  override fun <A> conquer(): Comparison<A> = Comparison { _, _ -> 0 }

  override fun <A, B, C> choose(f: (A) -> Either<B, C>, fb: K1<Comparison.T, B>, fc: K1<Comparison.T, C>): Comparison<A> =
    Comparison { a1, a2 -> compareEither(f(a1), f(a2), Comparison.narrow(fb), Comparison.narrow(fc)) }

  override fun <A> lose(f: (A) -> Nothing): Comparison<A> = Comparison { a, _ -> f(a) }
}

private fun <B, C> compareEither(e1: Either<B, C>, e2: Either<B, C>, fb: Comparison<B>, fc: Comparison<C>): Int = when {
  e1 is Either.Left && e2 is Either.Left -> fb.compare(e1.value, e2.value)
  e1 is Either.Right && e2 is Either.Right -> fc.compare(e1.value, e2.value)
  e1 is Either.Left -> -1
  else -> 1
}
