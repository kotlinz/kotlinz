package com.github.kotlinz.kotlinz.data.forget

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.const.Const
import com.github.kotlinz.kotlinz.data.const.ConstFunctor
import com.github.kotlinz.kotlinz.data.monoidof.MonoidOf
import com.github.kotlinz.kotlinz.type.applicative.Applicative
import com.github.kotlinz.kotlinz.type.profunctor.Traversing
import com.github.kotlinz.kotlinz.type.profunctor.Wander

/** Traversing instance for [Forget]: combines the results for every focus with the [monoid]. */
interface ForgetTraversing<R>: Traversing<Forget.T, R>, ForgetStrong<R>, ForgetChoice<R> {
  override fun <S, T, A, B> wander(traversal: Wander<S, T, A, B>, v: K3<Forget.T, R, A, B>): Forget<R, S, T> {
    val forget = Forget.narrow(v)
    val applicative = constApplicative(monoid)
    return Forget { s -> Const.narrow(traversal.run(applicative, { a: A -> Const<R, B>(forget.run(a)) }, s)).value }
  }
}

private fun <R> constApplicative(monoid: MonoidOf<R>) = object : Applicative<K1<Const.T, R>>, ConstFunctor<R> {
  override fun <A> pure(v: A): Const<R, A> = Const(monoid.empty)

  override fun <A, B> ap(f: K1<K1<Const.T, R>, (A) -> B>, v: K1<K1<Const.T, R>, A>): Const<R, B> =
    Const(monoid.combine(Const.narrow(f).value, Const.narrow(v).value))
}
