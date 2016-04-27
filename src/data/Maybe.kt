package data

import K1
import type.Monad

sealed class Maybe<A>: K1<Maybe.µ, A> {

  class µ {}

  class None<A>: Maybe<A>()
  class Just<A>(val value: A): Maybe<A>()

  companion object {
    fun <A> narrow(v: K1<µ, A>): Maybe<A> = v as Maybe<A>
    fun <A> pure(f: A): Maybe<A> = MaybeMonad.pure(f)
  }

  infix fun <B> fmap(f: (A) -> B): Maybe<B> = MaybeMonad.fmap(f, this)
  infix fun <B> ap(f: K1<µ, (A) -> B>): Maybe<B> = MaybeMonad.ap(f, this)
  infix fun <B> bind(f: (A) -> K1<µ, B>): Maybe<B> = MaybeMonad.bind(f, this)

  fun getOrElse(v: A): A {
    return when (this) {
      is Just -> this.value
      is None -> v
    }
  }

  private object MaybeMonad: Monad<µ> {
    override fun <B> pure(f: B): Maybe<B> = Just(f)

    override fun <A, B> fmap(f: (A) -> B, v: K1<µ, A>): Maybe<B> {
      val maybe = narrow(v)
      return when (maybe) {
        is Just -> Just(f(maybe.value))
        is None -> None()
      }
    }

    override fun <A, B> ap(f: K1<µ, (A) -> B>, v: K1<µ, A>): Maybe<B> {
      val maybe = narrow(v)
      val maybef = narrow(f)
      return when {
        maybe is Just && maybef is Just -> fmap(maybef.value, maybe)
        else -> None()
      }
    }

    override fun <A, B> bind(f: (A) -> K1<µ, B>, v: K1<µ, A>): Maybe<B> {
      val maybe = narrow(v)
      return when (maybe) {
        is Just -> narrow(f(maybe.value))
        is None -> None()
      }
    }
  }
}
