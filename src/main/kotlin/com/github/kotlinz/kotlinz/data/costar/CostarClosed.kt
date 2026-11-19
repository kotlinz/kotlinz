package com.github.kotlinz.kotlinz.data.costar

import com.github.kotlinz.kotlinz.K3
import com.github.kotlinz.kotlinz.type.profunctor.Closed

/** Closed instance for [Costar]: applies every function in the container to the shared input. */
interface CostarClosed<F>: Closed<Costar.T, F>, CostarProfunctor<F> {
  override fun <A, B, X> closed(v: K3<Costar.T, F, A, B>): Costar<F, (X) -> A, (X) -> B> {
    val costar = Costar.narrow(v)
    return Costar { fg -> { x -> costar.run(functor.fmap({ g: (X) -> A -> g(x) }, fg)) } }
  }
}
