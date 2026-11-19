package com.github.kotlinz.kotlinz.data.cont

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.MonadCont

/** MonadCont instance for [Cont]: the escape function discards its own continuation and resumes the outer one. */
interface ContMonadCont<R>: MonadCont<K1<Cont.T, R>>, ContMonad<R> {
  override fun <A, B> callCC(f: ((A) -> K1<K1<Cont.T, R>, B>) -> K1<K1<Cont.T, R>, A>): Cont<R, A> =
    Cont { k -> Cont.narrow(f { a -> Cont<R, B> { k(a) } }).run(k) }
}
