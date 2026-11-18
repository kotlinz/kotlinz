package com.github.kotlinz.kotlinz.data.io

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.Functor

/** Functor instance for [IO]. */
interface IOFunctor: Functor<IO.T> {
  override fun <A, B> fmap(f: (A) -> B, v: K1<IO.T, A>): IO<B> = IO { f(IO.narrow(v).unsafeRun()) }
}
