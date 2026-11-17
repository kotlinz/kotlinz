package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

interface IorFunctor<S>: Functor<K1<Ior.T, S>> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<K1<Ior.T, S>, A>): Ior<S, B> {
    return when (val ior = Ior.narrow(v)) {
      is Ior.Left -> Ior.Left(ior.value)
      is Ior.Right -> Ior.Right(f(ior.value))
      is Ior.Both -> Ior.Both(ior.left, f(ior.right))
    }
  }
}
