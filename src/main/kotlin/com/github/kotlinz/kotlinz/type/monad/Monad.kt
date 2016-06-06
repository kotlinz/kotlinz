package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface Monad<T>: Applicative<T> {
  fun <A> join(v: K1<T, K1<T, A>>): K1<T, A>
  fun <A, B> bind(f: (A) -> K1<T, B>, v: K1<T, A>): K1<T, B> = join(fmap(f, v))
}
