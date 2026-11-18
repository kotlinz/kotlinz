package com.github.kotlinz.kotlinz.data.codensity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.type.monad.Monad

/** Monad instance for [Codensity]. Binds are right-associated, so left-nested chains do not degrade. */
interface CodensityMonad<F>: Monad<K1<Codensity.T, F>>, CodensityApplicative<F>, CodensityFunctor<F> {
  override fun <A> join(v: K1<K1<Codensity.T, F>, K1<K1<Codensity.T, F>, A>>): Codensity<F, A> = bind({ it }, v)

  override fun <A, B> bind(f: (A) -> K1<K1<Codensity.T, F>, B>, v: K1<K1<Codensity.T, F>, A>): Codensity<F, B> = object: Codensity<F, B>() {
    override fun <R> run(k: (B) -> K1<F, R>): K1<F, R> = Codensity.narrow(v).run { a -> Codensity.narrow(f(a)).run(k) }
  }
}
