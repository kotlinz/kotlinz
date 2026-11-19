package com.github.kotlinz.kotlinz.data.io

import com.github.kotlinz.kotlinz.type.monad.MonadIO

/** MonadIO instance for [IO]: lifting is the identity. */
interface IOMonadIO: MonadIO<IO.T>, IOMonad {
  override fun <A> liftIO(v: IO<A>): IO<A> = v
}
