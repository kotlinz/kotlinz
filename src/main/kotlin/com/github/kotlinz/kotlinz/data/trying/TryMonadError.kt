package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadError

/** MonadError instance for [Try] with [Throwable] as the error type. */
interface TryMonadError: MonadError<Try.T, Throwable>, TryMonad {
  override fun <A> raiseError(e: Throwable): Try<A> = Try.Failure(e)

  override fun <A> handleErrorWith(f: (Throwable) -> K1<Try.T, A>, v: K1<Try.T, A>): Try<A> {
    val t = Try.narrow(v)
    return when (t) {
      is Try.Failure -> Try.narrow(f(t.error))
      is Try.Success -> t
    }
  }
}
