package data.maybe

import K1
import type.Monad

interface MaybeMonad: Monad<Maybe.µ> {
  override fun <B> pure(f: B): Maybe<B> = Maybe.Just(f)

  override fun <A, B> fmap(f: (A) -> B, v: K1<Maybe.µ, A>): Maybe<B> {
    val maybe = Maybe.narrow(v)
    return when (maybe) {
      is Maybe.Just -> Maybe.Just(f(maybe.value))
      is Maybe.None -> Maybe.None()
    }
  }

  override fun <A, B> ap(f: K1<Maybe.µ, (A) -> B>, v: K1<Maybe.µ, A>): Maybe<B> {
    val maybe = Maybe.narrow(v)
    val maybef = Maybe.narrow(f)
    return when {
      maybe is Maybe.Just && maybef is Maybe.Just -> fmap(maybef.value, maybe)
      else -> Maybe.None()
    }
  }

  override fun <A> join(v: K1<Maybe.µ, K1<Maybe.µ, A>>): Maybe<A> {
    val maybe = Maybe.narrow(v)
    return when (maybe) {
      is Maybe.Just -> Maybe.narrow(maybe.value)
      is Maybe.None -> Maybe.None()
    }
  }

  override fun <A, B> bind(f: (A) -> K1<Maybe.µ, B>, v: K1<Maybe.µ, A>): Maybe<B> {
    return Maybe.narrow(super.bind(f, v))
  }
}