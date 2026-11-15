package com.github.kotlinz.kotlinz.data.eval

import com.github.kotlinz.kotlinz.K1

class Eval<A>(private val thunk: () -> A): K1<Eval.T, A> {
  class T

  companion object {
    fun <A> narrow(v: K1<T, A>): Eval<A> = v as Eval<A>

    fun <A> now(v: A): Eval<A> = Eval { v }
    fun <A> later(f: () -> A): Eval<A> = Eval(f)

    // Monad
    fun <A> pure(v: A): Eval<A> = monad.pure(v)
    fun <A> join(v: K1<T, K1<T, A>>): Eval<A> = monad.join(v)
    private val monad = object: EvalMonad {}

    // ApplicativeOps
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: EvalApplicativeOps {}

    // MonadOps
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    fun <A, B, C> liftM2(f: (A, B) -> C) = monadOps.liftM2(f)
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) = monadOps.liftM3(f)
    private val monadOps = object: EvalMonadOps {}
  }

  val value: A by lazy { thunk() }

  // Monad
  infix fun <B> fmap(f: (A) -> B): Eval<B> = monad.fmap(f, this)
  infix fun <B> ap(f: Eval<(A) -> B>): Eval<B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> Eval<B>): Eval<B> = monad.bind(f, this)
  private val monad = object: EvalMonad {}

  // Comonad
  fun extract(): A = comonad.extract(this)
  fun duplicate(): Eval<K1<T, A>> = comonad.duplicate(this)
  fun <B> extend(f: (K1<T, A>) -> B): Eval<B> = comonad.extend(f, this)
  private val comonad = object: EvalComonad {}

  override fun equals(other: Any?): Boolean = other is Eval<*> && value == other.value
  override fun hashCode(): Int = value?.hashCode() ?: 0
}
