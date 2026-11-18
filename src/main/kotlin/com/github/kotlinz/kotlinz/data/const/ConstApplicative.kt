package com.github.kotlinz.kotlinz.data.const

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.group.Monoid

/** Applicative instance for [Const]. Requires A to be a Monoid; `pure` yields its identity and `ap` combines. */
interface ConstApplicative<S: Monoid<*, S>>: Applicative<K1<Const.T, S>>, ConstFunctor<S> {
  /** Class of the monoid used to combine values. */
  val type: Class<S>

  override fun <A> pure(v: A): Const<S, A> = Const(Monoid.mzero(type))

  override fun <A, B> ap(f: K1<K1<Const.T, S>, (A) -> B>, v: K1<K1<Const.T, S>, A>): Const<S, B> {
    return Const(Monoid.op(type, Const.narrow(f).value, Const.narrow(v).value))
  }
}
