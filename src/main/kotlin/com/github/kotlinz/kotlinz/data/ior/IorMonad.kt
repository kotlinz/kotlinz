package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.group.Semigroup
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [Ior]; requires a Semigroup class to accumulate left values and short-circuits on [Ior.Left]. */
interface IorMonad<S: Semigroup<*, S>>: Monad<K1<Ior.T, S>>, IorApplicative<S>, IorFunctor<S> {
  override fun <A> join(v: K1<K1<Ior.T, S>, K1<K1<Ior.T, S>, A>>): Ior<S, A> {
    return when (val ior = Ior.narrow(v)) {
      is Ior.Left -> Ior.Left(ior.value)
      is Ior.Right -> Ior.narrow(ior.value)
      is Ior.Both -> prependLeft(ior.left, Ior.narrow(ior.right))
    }
  }

  override fun <A, B> bind(f: (A) -> K1<K1<Ior.T, S>, B>, v: K1<K1<Ior.T, S>, A>): Ior<S, B> {
    return Ior.narrow(super.bind(f, v))
  }
}
