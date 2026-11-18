package com.github.kotlinz.kotlinz.data.predicate

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.divisible.Decidable

/**
 * Divisible and Decidable instance for [Predicate].
 *
 * `divide` requires both parts to pass (`&&`), `conquer` always passes, and `choose` tests the selected branch.
 */
interface PredicateDecidable: Decidable<Predicate.T>, PredicateContravariant {
  override fun <A, B, C> divide(f: (A) -> Pair<B, C>, fb: K1<Predicate.T, B>, fc: K1<Predicate.T, C>): Predicate<A> =
    Predicate { a -> f(a).let { (b, c) -> Predicate.narrow(fb).test(b) && Predicate.narrow(fc).test(c) } }

  override fun <A> conquer(): Predicate<A> = Predicate { true }

  override fun <A, B, C> choose(f: (A) -> Either<B, C>, fb: K1<Predicate.T, B>, fc: K1<Predicate.T, C>): Predicate<A> =
    Predicate { a ->
      when (val e = f(a)) {
        is Either.Left -> Predicate.narrow(fb).test(e.value)
        is Either.Right -> Predicate.narrow(fc).test(e.value)
      }
    }

  override fun <A> lose(f: (A) -> Nothing): Predicate<A> = Predicate(f)
}
