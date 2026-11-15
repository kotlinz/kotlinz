package com.github.kotlinz.kotlinz.data.cont

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2

class Cont<R, A>(val run: ((A) -> R) -> R): K2<Cont.T, R, A> {
  class T

  companion object {
    fun <R, A> narrow(v: K1<K1<T, R>, A>) = v as Cont<R, A>

    // Monad
    fun <R, A> pure(v: A): Cont<R, A> = monad<R>().pure(v)
    fun <R, A> join(v: K1<K1<T, R>, K1<K1<T, R>, A>>): Cont<R, A> = monad<R>().join(v)
    private fun <R> monad() = object: ContMonad<R> {}

    // ApplicativeOps
    fun <R, A, B> liftA(f: (A) -> B) = applicativeOps<R>().liftA(f)
    fun <R, A, B, C> liftA2(f: (A, B) -> C) = applicativeOps<R>().liftA2(f)
    fun <R, A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps<R>().liftA3(f)
    private fun <R> applicativeOps() = object: ContApplicativeOps<R> {}

    // MonadOps
    fun <R, A, B> liftM(f: (A) -> B) = monadOps<R>().liftM(f)
    fun <R, A, B, C> liftM2(f: (A,  B) -> C) =  monadOps<R>().liftM2(f)
    fun <R, A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps<R>().liftM3(f)
    private fun <R> monadOps() = object: ContMonadOps<R> {}
  }

  // Monad
  infix fun <B> fmap(f: (A) -> B): Cont<R, B> = monad.fmap(f, this)
  infix fun <B> ap(f: Cont<R, (A) -> B>): Cont<R, B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> Cont<R, B>): Cont<R, B> = monad.bind(f, this)
  private val monad = object: ContMonad<R> {}
}

fun <A> Cont<A, A>.eval(): A = run { it }

fun <R, A, B> callCC(f: ((A) -> Cont<R, B>) -> Cont<R, A>): Cont<R, A> {
  return Cont { k -> f { a -> Cont<R, B> { k(a) } }.run(k) }
}
