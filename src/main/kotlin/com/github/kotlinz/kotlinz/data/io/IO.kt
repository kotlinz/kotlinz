package com.github.kotlinz.kotlinz.data.io

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

class IO<A>(private val run: () -> A): K1<IO.T, A> {
  class T

  companion object {
    fun <A> narrow(v: K1<T, A>): IO<A> = v as IO<A>

    // Monad
    fun <A> pure(v: A): IO<A> = monad.pure(v)
    fun <A> join(v: K1<T, K1<T, A>>): IO<A> = monad.join(v)
    private val monad = object: IOMonad {}
    fun <A> fx(block: suspend MonadScope<T>.() -> A): IO<A> = narrow(monad.fx(block))

    // ApplicativeOps
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: IOApplicativeOps {}

    // MonadOps
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    fun <A, B, C> liftM2(f: (A, B) -> C) = monadOps.liftM2(f)
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) = monadOps.liftM3(f)
    private val monadOps = object: IOMonadOps {}
  }

  // Monad
  infix fun <B> fmap(f: (A) -> B): IO<B> = monad.fmap(f, this)
  infix fun <B> ap(f: IO<(A) -> B>): IO<B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> IO<B>): IO<B> = monad.bind(f, this)
  private val monad = object: IOMonad {}

  fun unsafeRun(): A = run()
}
