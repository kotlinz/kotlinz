package com.github.kotlinz.kotlinz.data.codensity

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.monad.Monad
import com.github.kotlinz.kotlinz.type.pointed.Pointed

abstract class Codensity<F, A>: K2<Codensity.T, F, A> {
  class T

  abstract fun <R> run(k: (A) -> K1<F, R>): K1<F, R>

  companion object {
    fun <F, A> narrow(v: K1<K1<T, F>, A>): Codensity<F, A> = v as Codensity<F, A>

    fun <F, A> lift(monad: Monad<F>, fa: K1<F, A>): Codensity<F, A> = object: Codensity<F, A>() {
      override fun <R> run(k: (A) -> K1<F, R>): K1<F, R> = monad.bind(k, fa)
    }

    // Monad
    fun <F, A> pure(v: A): Codensity<F, A> = monad<F>().pure(v)
    fun <F, A> join(v: K1<K1<T, F>, K1<K1<T, F>, A>>): Codensity<F, A> = monad<F>().join(v)
    private fun <F> monad() = object: CodensityMonad<F> {}
  }

  fun lower(pointed: Pointed<F>): K1<F, A> = run { a -> pointed.pure(a) }

  // Monad
  infix fun <B> fmap(f: (A) -> B): Codensity<F, B> = monad.fmap(f, this)
  infix fun <B> ap(f: Codensity<F, (A) -> B>): Codensity<F, B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> Codensity<F, B>): Codensity<F, B> = monad.bind(f, this)
  private val monad get() = object: CodensityMonad<F> {}
}
