package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1

interface Functor<T>: Invariant<T> {
  fun <A, B> fmap(f: (A) -> B, v: K1<T, A>): K1<T, B>
  override fun <A, B> imap(f: (A) -> B, g: (B) -> A, v: K1<T, A>): K1<T, B> = fmap(f, v)
}