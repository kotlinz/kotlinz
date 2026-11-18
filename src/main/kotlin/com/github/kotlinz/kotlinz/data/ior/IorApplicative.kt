package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Semigroup

/** Applicative instance for [Ior]; requires a Semigroup class [type] to accumulate left values. */
interface IorApplicative<S: Semigroup<*, S>>: Applicative<K1<Ior.T, S>> {
  /** Semigroup class used to combine left values. */
  val type: Class<S>

  override fun <A> pure(v: A): Ior<S, A> = Ior.Right(v)

  override fun <A, B> ap(f: K1<K1<Ior.T, S>, (A) -> B>, v: K1<K1<Ior.T, S>, A>): Ior<S, B> {
    return when (val iorf = Ior.narrow(f)) {
      is Ior.Left -> Ior.Left(iorf.value)
      is Ior.Right -> Ior.narrow(fmap(iorf.value, v))
      is Ior.Both -> prependLeft(iorf.left, Ior.narrow(fmap(iorf.right, v)))
    }
  }

  /** Combines [s] in front of the left value of [v], turning a [Ior.Right] into [Ior.Both]. */
  fun <A> prependLeft(s: S, v: Ior<S, A>): Ior<S, A> = when (v) {
    is Ior.Left -> Ior.Left(Semigroup.op(type, s, v.value))
    is Ior.Right -> Ior.Both(s, v.value)
    is Ior.Both -> Ior.Both(Semigroup.op(type, s, v.left), v.right)
  }
}
