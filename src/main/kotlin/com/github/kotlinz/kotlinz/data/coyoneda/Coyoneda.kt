package com.github.kotlinz.kotlinz.data.coyoneda

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor
import com.github.kotlinz.kotlinz.type.transformation.NaturalTransformation

// Existentially quantified over the hidden type of `fb`
@Suppress("UNCHECKED_CAST")
class Coyoneda<F, A> private constructor(val fb: K1<F, Any?>, val f: (Any?) -> A): K2<Coyoneda.T, F, A> {
  class T

  companion object {
    fun <F, A> narrow(v: K1<K1<T, F>, A>): Coyoneda<F, A> = v as Coyoneda<F, A>

    fun <F, B, A> of(fb: K1<F, B>, f: (B) -> A): Coyoneda<F, A> = Coyoneda(fb as K1<F, Any?>, f as (Any?) -> A)

    fun <F, A> lift(fa: K1<F, A>): Coyoneda<F, A> = of(fa) { it }
  }

  fun lower(functor: Functor<F>): K1<F, A> = functor.fmap(f, fb)

  fun <G> hoist(nt: NaturalTransformation<F, G>): Coyoneda<G, A> = of(nt(fb), f)

  // Functor
  infix fun <B> fmap(g: (A) -> B): Coyoneda<F, B> = functor.fmap(g, this)
  private val functor get() = object: CoyonedaFunctor<F> {}
}
