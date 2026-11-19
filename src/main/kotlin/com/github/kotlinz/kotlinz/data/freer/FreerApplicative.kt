package com.github.kotlinz.kotlinz.data.freer

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [Freer]; runs the function program first. */
interface FreerApplicative<F>: Applicative<K1<Freer.T, F>> {
  override fun <A> pure(v: A): Freer<F, A> = Freer.Pure(v)

  override fun <A, B> ap(f: K1<K1<Freer.T, F>, (A) -> B>, v: K1<K1<Freer.T, F>, A>): Freer<F, B> =
    Freer.narrow(f) bind { g -> Freer.narrow(v) fmap g }
}
