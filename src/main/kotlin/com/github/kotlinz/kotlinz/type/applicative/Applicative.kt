package com.github.kotlinz.kotlinz.type.applicative

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.pointed.Pointed

interface Applicative<T>: Functor<T>, Pointed<T> {
  fun <A, B> ap(f: K1<T, (A) -> B>, v: K1<T, A>): K1<T, B>
}