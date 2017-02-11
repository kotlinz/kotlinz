package com.github.kotlinz.kotlinz.data.state

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2

class State<S, A>(val run: (S) -> Pair<A, S>): K2<State.T, S, A> {
  class T

  companion object {
    fun <S, A> narrow(v: K1<K1<T, S>, A>) = v as State<S, A>

    // Monad
    fun <S, A> pure(v: A): State<S, A> = monad<S>().pure(v)
    fun <S, A> join(v: K1<K1<T, S>, K1<K1<T, S>, A>>): State<S, A> = monad<S>().join(v)
    private fun <S> monad() = object: StateMonad<S> {}

    // ApplicativeOps
    fun <S, A, B> liftA(f: (A) -> B) = applicativeOps<S>().liftA(f)
    fun <S, A, B, C> liftA2(f: (A, B) -> C) = applicativeOps<S>().liftA2(f)
    fun <S, A, B, C, D> liftA3(f: (A, B, C) -> D) = applicativeOps<S>().liftA3(f)
    private fun <S> applicativeOps() = object: StateApplicativeOps<S> {}

    // MonadOps
    fun <S, A, B> liftM(f: (A) -> B) = monadOps<S>().liftM(f)
    fun <S, A, B, C> liftM2(f: (A,  B) -> C) =  monadOps<S>().liftM2(f)
    fun <S, A, B, C, D> liftM3(f: (A, B, C) -> D) =  monadOps<S>().liftM3(f)
    private fun <S> monadOps() = object: StateMonadOps<S> {}
  }

  // Monad
  infix fun <B> fmap(f: (A) -> B): State<S, B> = monad.fmap(f, this)
  infix fun <B> ap(f: State<S, (A) -> B>): State<S, B> = monad.ap(f, this)
  infix fun <B> bind(f: (A) -> State<S, B>): State<S, B> = monad.bind(f, this)
  private val monad = object: StateMonad<S> {}

  fun eval(s: S): A = run(s).first
  fun exec(s: S): S = run(s).second
}

fun <S> get(): State<S, S> = State { Pair(it, it) }
fun <S> put(s: S): State<S, Unit> = State { Pair(Unit, s) }
