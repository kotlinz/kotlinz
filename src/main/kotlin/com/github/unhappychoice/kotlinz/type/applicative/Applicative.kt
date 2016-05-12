package com.github.unhappychoice.kotlinz.type.applicative

import com.github.unhappychoice.kotlinz.K1
import com.github.unhappychoice.kotlinz.type.Functor
import com.github.unhappychoice.kotlinz.type.pointed.Pointed

interface Applicative<T>: Functor<T>, Pointed<T> {
  fun <A, B> ap(f: K1<T, (A) -> B>, v: K1<T, A>): K1<T, B>
}