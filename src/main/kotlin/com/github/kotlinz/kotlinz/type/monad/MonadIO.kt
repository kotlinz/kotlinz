package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.io.IO

/**
 * A monad that can embed [IO] computations, typically a transformer stack whose base is `IO`.
 *
 * Laws:
 * - `liftIO(IO.pure(a)) == pure(a)`
 * - `liftIO(io bind f) == bind({ a -> liftIO(f(a)) }, liftIO(io))`
 *
 * @see MonadTrans
 */
interface MonadIO<T>: Monad<T> {
  /** Embeds an [IO] computation into this monad. */
  fun <A> liftIO(v: IO<A>): K1<T, A>
}
