package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [Try]. */
interface TryMonad: Monad<Try.T>, TryApplicative, TryFunctor {
  override fun <A> join(v: K1<Try.T, K1<Try.T, A>>): Try<A> {
    val t = Try.narrow(v)
    return when (t) {
      is Try.Success -> Try.narrow(t.value)
      is Try.Failure -> Try.Failure(t.error)
    }
  }

  override fun <A, B> bind(f: (A) -> K1<Try.T, B>, v: K1<Try.T, A>): Try<B> {
    return Try.narrow(super.bind(f, v))
  }
}
