package com.github.kotlinz.kotlinz.data.star

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.data.function.Function1
import com.github.kotlinz.kotlinz.data.function.Function1Functor
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.distributive.Distributive
import com.github.kotlinz.kotlinz.type.profunctor.Closed

/** Closed instance for [Star], distributing the effect out of the function with [distributive]. */
interface StarClosed<F>: Closed<Star.T, F>, StarProfunctor<F> {
  val distributive: Distributive<F>
  override val functor: Functor<F> get() = distributive

  override fun <A, B, X> closed(v: K3<Star.T, F, A, B>): Star<F, (X) -> A, (X) -> B> {
    val star = Star.narrow(v)
    return Star { g ->
      val distributed = distributive.distribute(object : Function1Functor<X> {}, Function1 { x: X -> star.run(g(x)) })
      distributive.fmap({ fb -> Function1.narrow(fb).run }, distributed)
    }
  }
}
