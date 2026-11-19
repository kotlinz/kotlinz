package com.github.kotlinz.kotlinz.data.freer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [Freer]; `bind` appends to the continuation queue in O(1). */
interface FreerMonad<F>: Monad<K1<Freer.T, F>>, FreerApplicative<F>, FreerFunctor<F> {
  override fun <A> join(v: K1<K1<Freer.T, F>, K1<K1<Freer.T, F>, A>>): Freer<F, A> = bind({ it }, v)

  @Suppress("UNCHECKED_CAST")
  override fun <A, B> bind(f: (A) -> K1<K1<Freer.T, F>, B>, v: K1<K1<Freer.T, F>, A>): Freer<F, B> =
    when (val freer = Freer.narrow(v)) {
      is Freer.Pure -> Freer.narrow(f(freer.value))
      is Freer.Impure -> Freer.Impure(freer.instruction, freer.arrows append FreerArrows.Leaf { x -> f(x as A) as Freer<F, Any?> })
    }
}
