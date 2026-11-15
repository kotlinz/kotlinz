package com.github.kotlinz.kotlinz.data.io

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

interface IOMonad: Monad<IO.T>, IOApplicative, IOFunctor {
  override fun <A> join(v: K1<IO.T, K1<IO.T, A>>): IO<A> {
    return IO { IO.narrow(IO.narrow(v).unsafeRun()).unsafeRun() }
  }

  override fun <A, B> bind(f: (A) -> K1<IO.T, B>, v: K1<IO.T, A>): IO<B> {
    return IO.narrow(super.bind(f, v))
  }
}
