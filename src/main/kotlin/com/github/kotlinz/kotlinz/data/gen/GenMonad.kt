package com.github.kotlinz.kotlinz.data.gen

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [Gen]; the next generator runs with the seed left by the previous one. */
interface GenMonad: Monad<Gen.T>, GenApplicative, GenFunctor {
  override fun <A> join(v: K1<Gen.T, K1<Gen.T, A>>): Gen<A> = bind({ it }, v)

  override fun <A, B> bind(f: (A) -> K1<Gen.T, B>, v: K1<Gen.T, A>): Gen<B> {
    return Gen(Gen.narrow(v).state bind { a -> Gen.narrow(f(a)).state })
  }
}
