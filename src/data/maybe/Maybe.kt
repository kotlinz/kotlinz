package data.maybe

import K1

sealed class Maybe<A>: K1<Maybe.µ, A> {

  class µ {}

  class None<A>: Maybe<A>()
  class Just<A>(val value: A): Maybe<A>()

  companion object {
    fun <A> narrow(v: K1<µ, A>): Maybe<A> = v as Maybe<A>

    // Monad
    fun <A> pure(f: A): Maybe<A> = (object: MaybeMonad {}).pure(f)
    fun <A> join(v: K1<µ, K1<µ, A>>): Maybe<A> = monad.join(v)
    private val monad = object: MaybeMonad {}

    // ApplicativeOps
    fun <A, B> liftA(f: (A) -> B) = applicativeOps.liftA(f)
    fun <A, B, C> liftA2(f: (A, B) -> C) = applicativeOps.liftA2(f)
    fun <A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps.liftA3(f)
    private val applicativeOps = object: MaybeApplicativeOps {}

    // MonadOps
    fun <A, B> liftM(f: (A) -> B) = monadOps.liftM(f)
    fun <A, B, C> liftM2(f: (A,  B) -> C) =  monadOps.liftM2(f)
    fun <A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps.liftM3(f)
    private val monadOps = object: MaybeMonadOps {}
  }

  // Monad
  infix fun <B> fmap(f: (A) -> B): Maybe<B> = monad.fmap(f, this)
  infix fun <B> ap(f: K1<µ, (A) -> B>): Maybe<B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> K1<µ, B>): Maybe<B> = monad.bind(f, this)
  private val monad = object: MaybeMonad {}

  fun getOrElse(v: A): A {
    return when (this) {
      is Just -> this.value
      is None -> v
    }
  }
}
