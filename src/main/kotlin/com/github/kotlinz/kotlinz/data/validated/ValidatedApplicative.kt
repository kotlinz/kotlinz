package com.github.kotlinz.kotlinz.data.validated

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Semigroup

interface ValidatedApplicative<E: Semigroup<*, E>>: Applicative<K1<Validated.T, E>>, ValidatedFunctor<E> {
  val type: Class<E>

  override fun <A> pure(v: A): Validated<E, A> = Validated.Valid(v)

  override fun <A, B> ap(f: K1<K1<Validated.T, E>, (A) -> B>, v: K1<K1<Validated.T, E>, A>): Validated<E, B> {
    val validated = Validated.narrow(v)
    return when (val validatedf = Validated.narrow(f)) {
      is Validated.Valid -> fmap(validatedf.value, validated)
      is Validated.Invalid -> Validated.Invalid(accumulate(validatedf.error, validated))
    }
  }

  private fun <A> accumulate(error: E, v: Validated<E, A>): E = when (v) {
    is Validated.Valid -> error
    is Validated.Invalid -> Semigroup.op(type, error, v.error)
  }
}
