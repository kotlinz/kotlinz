package com.github.kotlinz.kotlinz.data.maybe

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx

sealed class Maybe<A>: K1<Maybe.T, A> {

  class T

  class None<A>: Maybe<A>()
  class Just<A>(val value: A): Maybe<A>()

  companion object {
    fun <A> narrow(v: K1<T, A>): Maybe<A> = v as Maybe<A>

    // Monad
    fun <A> pure(f: A): Maybe<A> = monad.pure(f)
    fun <A> join(v: K1<T, K1<T, A>>): Maybe<A> = monad.join(v)
    private val monad = object: MaybeMonad {}
    fun <A> fx(block: suspend MonadScope<T>.() -> A): Maybe<A> = narrow(monad.fx(block))

    // ApplicativeOps
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: MaybeApplicativeOps {}

    // Alternative
    fun <A> empty(): Maybe<A> = alternative.empty()
    private val alternative = object: MaybeAlternative {}

    // MonadPlus
    fun <A> mzero(): Maybe<A> = monadPlus.mzero()
    private val monadPlus = object: MaybeMonadPlus {}

    // MonadOps
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    fun <A, B, C> liftM2(f: (A,  B) -> C) =  monadOps.liftM2(f)
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps.liftM3(f)
    private val monadOps = object: MaybeMonadOps {}
  }

  // Monad
  infix fun <B> fmap(f: (A) -> B): Maybe<B> = monad.fmap(f, this)
  infix fun <B> ap(f: K1<T, (A) -> B>): Maybe<B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> K1<T, B>): Maybe<B> = monad.bind(f, this)
  private val monad = object: MaybeMonad {}

  fun getOrElse(v: A): A {
    return when (this) {
      is Just -> this.value
      is None -> v
    }
  }

  // Alternative
  infix fun orElse(m: K1<T, A>): Maybe<A> = alternative.orElse(this, m)
  fun optional(): Maybe<Maybe<A>> = narrow(alternative.optional(this))
  private val alternative = object: MaybeAlternative {}

  // MonadPlus
  infix fun mplus(m: K1<T, A>): Maybe<A> = monadPlus.mplus(this, m)
  private val monadPlus = object: MaybeMonadPlus {}

  override fun equals(other: Any?): Boolean {
    return when {
      this is Just && other is Maybe.Just<*> -> this.value == other.value
      this is None && other is Maybe.None<*> -> true
      else -> false
    }
  }
}
