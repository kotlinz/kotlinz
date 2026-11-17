package com.github.kotlinz.kotlinz.type.representable

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface Representable<F, Rep>: Functor<F> {
  fun <A> tabulate(f: (Rep) -> A): K1<F, A>
  fun <A> index(fa: K1<F, A>): (Rep) -> A
}
