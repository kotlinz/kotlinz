package com.github.kotlinz.kotlinz.data.either

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.maybe.Maybe
import com.github.kotlinz.kotlinz.type.Filterable
import com.github.kotlinz.kotlinz.type.group.Monoid

/** Filterable instance for [Either] with a monoidal left type; a dropped [Either.Right] becomes `Left(mzero)`. */
interface EitherFilterable<L: Monoid<*, L>>: Filterable<K1<Either.T, L>>, EitherFunctor<L> {
  /** The monoid class of the left type, used to produce the empty left value. */
  val type: Class<L>

  override fun <A, B> mapFilter(f: (A) -> Maybe<B>, v: K1<K1<Either.T, L>, A>): Either<L, B> {
    return when (val either = Either.narrow(v)) {
      is Either.Left -> Either.Left(either.value)
      is Either.Right -> fromMaybe(f(either.value))
    }
  }

  override fun <A> filter(p: (A) -> Boolean, v: K1<K1<Either.T, L>, A>): Either<L, A> = Either.narrow(super.filter(p, v))
  override fun <A, B: Any> collect(f: (A) -> B?, v: K1<K1<Either.T, L>, A>): Either<L, B> = Either.narrow(super.collect(f, v))
  override fun <A> catMaybes(v: K1<K1<Either.T, L>, Maybe<A>>): Either<L, A> = Either.narrow(super.catMaybes(v))

  private fun <B> fromMaybe(m: Maybe<B>): Either<L, B> = when (m) {
    is Maybe.Just -> Either.Right(m.value)
    is Maybe.None -> Either.Left(Monoid.mzero(type))
  }
}
