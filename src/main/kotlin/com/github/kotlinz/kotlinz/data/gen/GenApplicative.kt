package com.github.kotlinz.kotlinz.data.gen

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.state.State
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [Gen]; the function generator consumes the seed first. */
interface GenApplicative: Applicative<Gen.T>, GenFunctor {
  override fun <A> pure(v: A): Gen<A> = Gen(State.pure(v))

  override fun <A, B> ap(f: K1<Gen.T, (A) -> B>, v: K1<Gen.T, A>): Gen<B> {
    return Gen(Gen.narrow(v).state ap Gen.narrow(f).state)
  }
}
