package data.identity

import K1

data class Identity<A>(val value: A): K1<Identity.µ, A> {
  class µ {}

  companion object {
    fun <A> narrow(i: K1<µ, A>): Identity<A> = i as Identity<A>
    fun <A> pure(v: A): Identity<A> = (object: IdentityMonad {}).pure(v)
    fun <A> join(v: K1<µ, K1<µ, A>>): Identity<A> = (object: IdentityMonad {}).join(v)
  }

  fun extract(): A = copointed.extract(this)

  infix fun <B> fmap(f: (A) -> B): Identity<B> = monad.fmap(f, this)
  infix fun <B> ap(f: Identity<(A) -> B>): Identity<B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> Identity<B>): Identity<B> = monad.bind(f, this)

  private val copointed = object: IdentityCopointed {}
  private val monad = object: IdentityMonad {}
}

