package com.github.kotlinz.kotlinz.data.function

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

class Function0<A>(val run: () -> A): K1<Function0.T, A> {
  class T

  companion object {
    fun <A> narrow(v: K1<T, A>): Function0<A> = v as Function0<A>

    // Monad
    fun <A> pure(v: A): Function0<A> = monad.pure(v)
    fun <A> join(v: K1<T, K1<T, A>>): Function0<A> = monad.join(v)
    private val monad = object: Function0Monad {}
    fun <A> fx(block: suspend MonadScope<T>.() -> A): Function0<A> = narrow(monad.fx(block))

    // ApplicativeOps
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: Function0ApplicativeOps {}

    // MonadOps
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    fun <A, B, C> liftM2(f: (A, B) -> C) = monadOps.liftM2(f)
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) = monadOps.liftM3(f)
    private val monadOps = object: Function0MonadOps {}
  }

  operator fun invoke(): A = run()

  // Monad
  infix fun <B> fmap(f: (A) -> B): Function0<B> = monad.fmap(f, this)
  infix fun <B> ap(f: Function0<(A) -> B>): Function0<B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> Function0<B>): Function0<B> = monad.bind(f, this)
  private val monad = object: Function0Monad {}

  // Comonad
  fun extract(): A = comonad.extract(this)
  fun duplicate(): Function0<K1<T, A>> = comonad.duplicate(this)
  fun <B> extend(f: (K1<T, A>) -> B): Function0<B> = comonad.extend(f, this)
  private val comonad = object: Function0Comonad {}
}
