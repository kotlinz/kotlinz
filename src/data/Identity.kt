package data

import K1
import type.CoPointed
import type.Monad

data class Identity<A>(private val value: A): K1<Identity.µ, A> {
  class µ {}

  companion object {
    fun <A> narrow(i: K1<µ, A>): Identity<A> = i as Identity<A>
    fun <B> pure(v: B): Identity<B> = IdentityMonad.pure(v)
  }

  fun extract(): A = IdentityCoPointed.extract(this)

  infix fun <B> fmap(f: (A) -> B): Identity<B> = IdentityMonad.fmap(f, this)
  infix fun <B> ap(f: Identity<(A) -> B>): Identity<B> = IdentityMonad.ap(f, this)
  infix fun <B> bind(f: (A) -> Identity<B>): Identity<B> = IdentityMonad.bind(f, this)

  private object IdentityCoPointed: CoPointed<µ> {
    override fun <A> extract(v: K1<µ, A>): A = narrow(v).value
  }

  private object IdentityMonad: Monad<µ> {
    override fun <A> pure(v: A): Identity<A> = Identity(v)
    override fun <A, B> fmap(f: (A) -> B, v: K1<µ, A>): Identity<B> = Identity(f(narrow(v).value))
    override fun <A, B> ap(f: K1<µ, (A) -> B>, v: K1<µ, A>): Identity<B> = fmap(narrow(f).value, v)
    override fun <A, B> bind(f: (A) -> K1<µ, B>, v: K1<µ, A>): Identity<B> = narrow(f(narrow(v).value))
  }
}

