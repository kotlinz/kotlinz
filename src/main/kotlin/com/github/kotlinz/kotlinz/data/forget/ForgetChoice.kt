package com.github.kotlinz.kotlinz.data.forget

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.data.monoidof.MonoidOf
import com.github.kotlinz.kotlinz.type.profunctor.Choice

/** Choice instance for [Forget]: the skipped branch yields the [monoid]'s empty value. */
interface ForgetChoice<R>: Choice<Forget.T, R>, ForgetProfunctor<R> {
  val monoid: MonoidOf<R>

  override fun <A, B, C> left(v: K3<Forget.T, R, A, B>): Forget<R, Either<A, C>, Either<B, C>> {
    val forget = Forget.narrow(v)
    return Forget { e -> if (e is Either.Left) forget.run(e.value) else monoid.empty }
  }

  override fun <A, B, C> right(v: K3<Forget.T, R, A, B>): Forget<R, Either<C, A>, Either<C, B>> {
    val forget = Forget.narrow(v)
    return Forget { e -> if (e is Either.Right) forget.run(e.value) else monoid.empty }
  }
}
