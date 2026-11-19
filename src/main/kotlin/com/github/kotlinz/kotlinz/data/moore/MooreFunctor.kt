package com.github.kotlinz.kotlinz.data.moore

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Moore] with a fixed input type [I]; maps every output. */
interface MooreFunctor<I>: Functor<K2<Moore.T, Unit, I>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K2<Moore.T, Unit, I>, A>): Moore<I, B> {
    val moore = Moore.narrow(v)
    return Moore(f(moore.output)) { i -> fmap(f, moore.step(i)) }
  }
}
