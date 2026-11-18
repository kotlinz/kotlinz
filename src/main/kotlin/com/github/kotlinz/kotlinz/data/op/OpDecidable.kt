package com.github.kotlinz.kotlinz.data.op

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.type.divisible.Decidable
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * Divisible and Decidable instance for [Op] whose result `R` is a Monoid.
 *
 * `divide` combines both results with the Monoid of [type]; `conquer` returns its empty value.
 */
interface OpDecidable<R: Monoid<*, R>>: Decidable<K1<Op.T, R>>, OpContravariant<R> {
  /** Runtime class of the result Monoid `R`. */
  val type: Class<R>

  override fun <A, B, C> divide(f: (A) -> Pair<B, C>, fb: K1<K1<Op.T, R>, B>, fc: K1<K1<Op.T, R>, C>): Op<R, A> =
    Op { a -> f(a).let { (b, c) -> Monoid.op(type, Op.narrow(fb).run(b), Op.narrow(fc).run(c)) } }

  override fun <A> conquer(): Op<R, A> = Op { Monoid.mzero(type) }

  override fun <A, B, C> choose(f: (A) -> Either<B, C>, fb: K1<K1<Op.T, R>, B>, fc: K1<K1<Op.T, R>, C>): Op<R, A> =
    Op { a ->
      when (val e = f(a)) {
        is Either.Left -> Op.narrow(fb).run(e.value)
        is Either.Right -> Op.narrow(fc).run(e.value)
      }
    }

  override fun <A> lose(f: (A) -> Nothing): Op<R, A> = Op(f)
}
