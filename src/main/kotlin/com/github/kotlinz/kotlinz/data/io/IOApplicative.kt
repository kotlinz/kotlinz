package com.github.kotlinz.kotlinz.data.io

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

interface IOApplicative: Applicative<IO.T> {
  override fun <A> pure(v: A): IO<A> = IO { v }

  override fun <A, B> ap(f: K1<IO.T, (A) -> B>, v: K1<IO.T, A>): IO<B> {
    return IO { IO.narrow(f).unsafeRun()(IO.narrow(v).unsafeRun()) }
  }
}
