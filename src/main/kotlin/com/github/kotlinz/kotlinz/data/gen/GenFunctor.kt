package com.github.kotlinz.kotlinz.data.gen

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [Gen]; maps the generated value without consuming extra randomness. */
interface GenFunctor: Functor<Gen.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<Gen.T, A>): Gen<B> = Gen(Gen.narrow(v).state fmap f)
}
