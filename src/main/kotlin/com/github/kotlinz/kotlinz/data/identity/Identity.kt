package com.github.kotlinz.kotlinz.data.identity

import com.github.kotlinz.kotlinz.K1

data class Identity<A>(val value: A): K1<Identity.T, A> {
  class T

  companion object {
    fun <A> narrow(i: K1<T, A>): Identity<A> = i as Identity<A>

    // Monad
    fun <A> pure(v: A): Identity<A> = monad.pure(v)
    fun <A> join(v: K1<T, K1<T, A>>): Identity<A> = monad.join(v)
    private val monad = object: IdentityMonad {}

    // ApplicativeOps1
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: IdentityApplicativeOps {}

    // MonadOps
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    fun <A, B, C> liftM2(f: (A,  B) -> C) =  monadOps.liftM2(f)
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps.liftM3(f)
    private val monadOps = object: IdentityMonadOps {}

    // MonadZip
    infix fun <A, B> munzip(m: K1<T, Pair<A, B>>): Pair<Identity<A>, Identity<B>> = monadZip.munzip(m)
    private val monadZip = object: IdentityMonadZip {}
  }

  // Monad
  infix fun <B> fmap(f: (A) -> B): Identity<B> = monad.fmap(f, this)
  infix fun <B> ap(f: Identity<(A) -> B>): Identity<B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> Identity<B>): Identity<B> = monad.bind(f, this)
  private val monad = object: IdentityMonad {}

  // MonadZip
  infix fun <B> mzip(m: K1<T, B>): Identity<Pair<A, B>> = monadZip.mzip(this, m)
  fun <B, C> mzipWith(m: K1<T, B>, f: (A,  B) -> C): Identity<C> = monadZip.mzipWith(this, m, f)
  private val monadZip = object: IdentityMonadZip {}

  // Comonad
  fun extract(): A = comonad.extract(this)
  fun duplicate(): Identity<K1<T, A>> = comonad.duplicate(this)
  fun <B> extend(f: (K1<T, A>) -> B): Identity<B> = comonad.extend(f, this)
  private val comonad = object: IdentityComonad {}
}

