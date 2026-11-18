package com.github.kotlinz.kotlinz.data.io

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadError

/** MonadError instance for [IO] with [Throwable] errors; catches every Throwable, including fatal ones. */
interface IOMonadError: MonadError<IO.T, Throwable>, IOMonad {
  override fun <A> raiseError(e: Throwable): IO<A> = IO { throw e }

  override fun <A> handleErrorWith(f: (Throwable) -> K1<IO.T, A>, v: K1<IO.T, A>): IO<A> {
    return IO {
      try {
        IO.narrow(v).unsafeRun()
      } catch (e: Throwable) {
        IO.narrow(f(e)).unsafeRun()
      }
    }
  }
}
