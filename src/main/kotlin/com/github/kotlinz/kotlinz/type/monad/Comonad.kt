package com.github.kotlinz.kotlinz.type.monad

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.pointed.Copointed

interface Comonad<T>: Copointed<T>, Functor<T> {
  fun <A> duplicate(v: K1<T, A>): K1<T, K1<T, A>>
  fun <A, B> extend(f: (K1<T, A>) -> B, v: K1<T, A>): K1<T, B> = fmap(f, duplicate(v))
}