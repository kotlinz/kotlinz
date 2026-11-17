package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1

interface Invariant<T> {
  fun <A, B> imap(f: (A) -> B, g: (B) -> A, v: K1<T, A>): K1<T, B>
}
