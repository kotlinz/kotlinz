package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

interface MonadReader<T, R>: Monad<T> {
  fun ask(): K1<T, R>
  fun <A> local(f: (R) -> R, v: K1<T, A>): K1<T, A>

  fun <A> reader(f: (R) -> A): K1<T, A> = fmap(f, ask())
}
