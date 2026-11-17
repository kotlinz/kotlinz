package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

interface MonadWriter<T, W>: Monad<T> {
  fun <A> writer(v: Pair<A, W>): K1<T, A>
  fun <A> listen(v: K1<T, A>): K1<T, Pair<A, W>>
  fun <A> pass(v: K1<T, Pair<A, (W) -> W>>): K1<T, A>

  fun tell(w: W): K1<T, Unit> = writer(Pair(Unit, w))

  fun <A, B> listens(f: (W) -> B, v: K1<T, A>): K1<T, Pair<A, B>> {
    return fmap({ (a, w) -> Pair(a, f(w)) }, listen(v))
  }

  fun <A> censor(f: (W) -> W, v: K1<T, A>): K1<T, A> = pass(fmap({ a -> Pair(a, f) }, v))
}
