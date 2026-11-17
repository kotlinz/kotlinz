package com.github.kotlinz.kotlinz.data.yoneda

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.Functor

abstract class Yoneda<F, A>: K2<Yoneda.T, F, A> {
  class T

  abstract fun <B> run(f: (A) -> B): K1<F, B>

  companion object {
    fun <F, A> narrow(v: K1<K1<T, F>, A>): Yoneda<F, A> = v as Yoneda<F, A>

    fun <F, A> lift(functor: Functor<F>, fa: K1<F, A>): Yoneda<F, A> = object: Yoneda<F, A>() {
      override fun <B> run(f: (A) -> B): K1<F, B> = functor.fmap(f, fa)
    }
  }

  fun lower(): K1<F, A> = run { it }

  // Functor
  infix fun <B> fmap(f: (A) -> B): Yoneda<F, B> = functor.fmap(f, this)
  private val functor get() = object: YonedaFunctor<F> {}
}
