package com.github.kotlinz.kotlinz.data.moore

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Comonad

/** Comonad instance for [Moore]; [duplicate] exposes every reachable machine as an output. */
interface MooreComonad<I>: Comonad<K2<Moore.T, Unit, I>>, MooreCopointed<I>, MooreFunctor<I> {
  override fun <A> duplicate(v: K1<K2<Moore.T, Unit, I>, A>): Moore<I, K1<K2<Moore.T, Unit, I>, A>> {
    val moore = Moore.narrow(v)
    return Moore(moore) { i -> duplicate(moore.step(i)) }
  }

  override fun <A, B> extend(f: (K1<K2<Moore.T, Unit, I>, A>) -> B, v: K1<K2<Moore.T, Unit, I>, A>): Moore<I, B> =
    Moore.narrow(super.extend(f, v))
}
