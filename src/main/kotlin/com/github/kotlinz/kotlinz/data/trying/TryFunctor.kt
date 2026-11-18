package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Try]; exceptions thrown by the mapped function become [Try.Failure]. */
interface TryFunctor: Functor<Try.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Try.T, A>): Try<B> {
    val t = Try.narrow(v)
    return when (t) {
      is Try.Success -> Try.of { f(t.value) }
      is Try.Failure -> Try.Failure(t.error)
    }
  }
}
