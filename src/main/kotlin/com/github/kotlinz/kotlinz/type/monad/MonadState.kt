package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1

interface MonadState<T, S>: Monad<T> {
  fun get(): K1<T, S>
  fun put(s: S): K1<T, Unit>

  fun modify(f: (S) -> S): K1<T, Unit> = bind({ s -> put(f(s)) }, get())
  fun <A> gets(f: (S) -> A): K1<T, A> = fmap(f, get())

  fun <A> state(f: (S) -> Pair<A, S>): K1<T, A> {
    return bind({ s -> f(s).let { (a, s2) -> fmap({ _: Unit -> a }, put(s2)) } }, get())
  }
}
