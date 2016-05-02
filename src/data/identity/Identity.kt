package data.identity

import K1

data class Identity<A>(val value: A): K1<Identity.µ, A> {
  class µ {}

  companion object {
    fun <A> narrow(i: K1<µ, A>): Identity<A> = i as Identity<A>

    fun <A> pure(v: A): Identity<A> = monad.pure(v)
    fun <A> join(v: K1<µ, K1<µ, A>>): Identity<A> = monad.join(v)

    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    fun <A, B, C> liftM2(f: (A,  B) -> C) =  monadOps.liftM2(f)
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps.liftM3(f)

    private val monad = object: IdentityMonad {}
    private val monadOps = object: IdentityMonadOps {}
  }

  fun extract(): A = copointed.extract(this)

  infix fun <B> fmap(f: (A) -> B): Identity<B> = monad.fmap(f, this)
  infix fun <B> ap(f: Identity<(A) -> B>): Identity<B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> Identity<B>): Identity<B> = monad.bind(f, this)

  private val copointed = object: IdentityCopointed {}
  private val monad = object: IdentityMonad {}
}

