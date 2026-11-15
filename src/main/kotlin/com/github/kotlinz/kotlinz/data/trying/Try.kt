package com.github.kotlinz.kotlinz.data.trying

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

sealed class Try<A>: K1<Try.T, A> {

  class T

  class Success<A>(val value: A): Try<A>()
  class Failure<A>(val error: Throwable): Try<A>()

  companion object {
    fun <A> narrow(v: K1<T, A>): Try<A> = v as Try<A>

    fun <A> of(f: () -> A): Try<A> {
      return try {
        Success(f())
      } catch (e: Exception) {
        Failure(e)
      }
    }

    // Monad
    fun <A> pure(v: A): Try<A> = monad.pure(v)
    fun <A> join(v: K1<T, K1<T, A>>): Try<A> = monad.join(v)
    private val monad = object: TryMonad {}
    fun <A> fx(block: suspend MonadScope<T>.() -> A): Try<A> = narrow(monad.fx(block))

    // ApplicativeOps
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: TryApplicativeOps {}

    // MonadOps
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    fun <A, B, C> liftM2(f: (A,  B) -> C) =  monadOps.liftM2(f)
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps.liftM3(f)
    private val monadOps = object: TryMonadOps {}
  }

  // Monad
  infix fun <B> fmap(f: (A) -> B): Try<B> = monad.fmap(f, this)
  infix fun <B> ap(f: K1<T, (A) -> B>): Try<B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> K1<T, B>): Try<B> = monad.bind(f, this)
  private val monad = object: TryMonad {}

  fun getOrElse(v: A): A {
    return when (this) {
      is Success -> this.value
      is Failure -> v
    }
  }

  override fun equals(other: Any?): Boolean {
    return when {
      this is Success && other is Try.Success<*> -> this.value == other.value
      this is Failure && other is Try.Failure<*> -> this.error == other.error
      else -> false
    }
  }
}
