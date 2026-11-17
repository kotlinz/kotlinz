package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Bifunctor

interface IorBifunctor : Bifunctor<Ior.T> {
  override fun <A, B, C, D> bimap(f: (A) -> C, g: (B) -> D, v: K2<Ior.T, A, B>): Ior<C, D> {
    return when (val ior = Ior.narrow(v)) {
      is Ior.Left -> Ior.Left(f(ior.value))
      is Ior.Right -> Ior.Right(g(ior.value))
      is Ior.Both -> Ior.Both(f(ior.left), g(ior.right))
    }
  }

  override fun <A, B, C> first(f: (A) -> C, v: K2<Ior.T, A, B>): Ior<C, B> = Ior.narrow(super.first(f, v))
  override fun <A, B, D> second(g: (B) -> D, v: K2<Ior.T, A, B>): Ior<A, D> = Ior.narrow(super.second(g, v))
}
