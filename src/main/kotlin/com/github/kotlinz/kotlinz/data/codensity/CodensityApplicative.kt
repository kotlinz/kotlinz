package com.github.kotlinz.kotlinz.data.codensity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.applicative.Applicative

/** Applicative instance for [Codensity]. */
interface CodensityApplicative<F>: Applicative<K1<Codensity.T, F>> {
  override fun <A> pure(v: A): Codensity<F, A> = object: Codensity<F, A>() {
    override fun <R> run(k: (A) -> K1<F, R>): K1<F, R> = k(v)
  }

  override fun <A, B> ap(f: K1<K1<Codensity.T, F>, (A) -> B>, v: K1<K1<Codensity.T, F>, A>): Codensity<F, B> = object: Codensity<F, B>() {
    override fun <R> run(k: (B) -> K1<F, R>): K1<F, R> =
      Codensity.narrow(f).run { g -> Codensity.narrow(v).run { a -> k(g(a)) } }
  }
}
