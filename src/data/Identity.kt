package data

import K1
import type.Monad

data class Identity<A>(val value: A): K1<Identity.µ, A> {
  class µ {}

  companion object {
    fun <A> narrow(i: K1<µ, A>): Identity<A> = i as Identity<A>
    fun <B> pure(v: B): Identity<B> = MonadInstance.pure(v)
  }

  infix fun <B> fmap(f: (A) -> B): Identity<B> = MonadInstance.fmap(f, this)
  infix fun <B> ap(f: Identity<(A) -> B>): Identity<B> = MonadInstance.ap(f, this)
  infix fun <B> bind(f: (A) -> Identity<B>): Identity<B> = MonadInstance.bind(f, this)

  private object MonadInstance: Monad<µ> {
    override fun <A> pure(v: A): Identity<A> = Identity(v)
    override fun <A, B> fmap(f: (A) -> B, v: K1<µ, A>): Identity<B> = Identity(f(narrow(v).value))
    override fun <A, B> ap(f: K1<µ, (A) -> B>, v: K1<µ, A>): Identity<B> = fmap(narrow(f).value, v)
    override fun <A, B> bind(f: (A) -> K1<µ, B>, v: K1<µ, A>): Identity<B> = narrow(f(narrow(v).value))
  }
}

