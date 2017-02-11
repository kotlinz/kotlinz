package com.github.kotlinz.kotlinz.type

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Monoid

interface Foldable<T> {
  fun <A, B> foldl(initial: A, f: ((A) -> B) -> A, v: K1<T, A>): A
  fun <A, B> foldr(initial: B, f: ((A) -> A) -> B, v: K1<T, B>): B
  fun <A, M: Monoid<T, M>> foldMap(f: (A) -> M, v: K1<T, A>): M
}
