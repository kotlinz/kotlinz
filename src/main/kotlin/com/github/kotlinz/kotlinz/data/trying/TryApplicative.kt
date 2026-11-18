package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [Try]; fails fast on the first [Try.Failure]. */
interface TryApplicative: Applicative<Try.T>, TryFunctor {
  override fun <B> pure(v: B): Try<B> = Try.Success(v)

  override fun <A, B> ap(f: K1<Try.T, (A) -> B>, v: K1<Try.T, A>): Try<B> {
    val t = Try.narrow(v)
    val tf = Try.narrow(f)
    return when (tf) {
      is Try.Success -> Try.narrow(fmap(tf.value, t))
      is Try.Failure -> Try.Failure(tf.error)
    }
  }
}
