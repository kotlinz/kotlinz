package data.maybe

import K1

sealed class Maybe<A>: K1<Maybe.µ, A> {

  class µ {}

  class None<A>: Maybe<A>()
  class Just<A>(val value: A): Maybe<A>()

  companion object {
    fun <A> narrow(v: K1<µ, A>): Maybe<A> = v as Maybe<A>
    fun <A> pure(f: A): Maybe<A> = (object: MaybeMonad {}).pure(f)
  }

  infix fun <B> fmap(f: (A) -> B): Maybe<B> = monad.fmap(f, this)
  infix fun <B> ap(f: K1<µ, (A) -> B>): Maybe<B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> K1<µ, B>): Maybe<B> = monad.bind(f, this)

  fun getOrElse(v: A): A {
    return when (this) {
      is Just -> this.value
      is None -> v
    }
  }

  private val monad = object: MaybeMonad {}
}
