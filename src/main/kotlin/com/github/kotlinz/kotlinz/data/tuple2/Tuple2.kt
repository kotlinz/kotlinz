package com.github.kotlinz.kotlinz.data.tuple2

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.group.Monoid

data class Tuple2<A, B>(val first: A, val second: B): K2<Tuple2.T, A, B> {
  class T

  companion object {
    fun <A, B> narrow(v: K1<K1<T, A>, B>) = v as Tuple2<A, B>

    // Monad
    fun <S: Monoid<*, S>, A> pure(type: Class<S>, v: A): Tuple2<S, A> = monad(type).pure(v)
    fun <S: Monoid<*, S>, A> join(type: Class<S>, v: K1<K1<T, S>, K1<K1<T, S>, A>>): Tuple2<S, A> = monad(type).join(v)
    internal fun <S: Monoid<*, S>> monad(type: Class<S>) = object: Tuple2Monad<S> { override val type = type }
    fun <S: Monoid<*, S>, A> fx(type: Class<S>, block: suspend MonadScope<K1<T, S>>.() -> A): Tuple2<S, A> = narrow(monad(type).fx(block))

    // ApplicativeOps
    fun <S: Monoid<*, S>, A, B> liftA(type: Class<S>, f: (A) -> B) = applicativeOps(type).liftA(f)
    fun <S: Monoid<*, S>, A, B, C> liftA2(type: Class<S>, f: (A, B) -> C) = applicativeOps(type).liftA2(f)
    fun <S: Monoid<*, S>, A, B, C, D> liftA3(type: Class<S>, f: (A, B, C) -> D) = applicativeOps(type).liftA3(f)
    private fun <S: Monoid<*, S>> applicativeOps(type: Class<S>) = object: Tuple2ApplicativeOps<S> { override val type = type }

    // MonadOps
    fun <S: Monoid<*, S>, A, B> liftM(type: Class<S>, f: (A) -> B) = monadOps(type).liftM(f)
    fun <S: Monoid<*, S>, A, B, C> liftM2(type: Class<S>, f: (A, B) -> C) = monadOps(type).liftM2(f)
    fun <S: Monoid<*, S>, A, B, C, D> liftM3(type: Class<S>, f: (A, B, C) -> D) = monadOps(type).liftM3(f)
    private fun <S: Monoid<*, S>> monadOps(type: Class<S>) = object: Tuple2MonadOps<S> { override val type = type }
  }

  // Functor
  infix fun <C> fmap(f: (B) -> C): Tuple2<A, C> = comonad.fmap(f, this)

  // Comonad
  fun extract(): B = comonad.extract(this)
  fun duplicate(): Tuple2<A, K1<K1<T, A>, B>> = comonad.duplicate(this)
  fun <C> extend(f: (K1<K1<T, A>, B>) -> C): Tuple2<A, C> = comonad.extend(f, this)
  private val comonad = object: Tuple2Comonad<A> {}

  // Bifunctor (first / second are omitted as members because they clash with the properties)
  fun <C, D> bimap(f: (A) -> C, g: (B) -> D): Tuple2<C, D> = bifunctor.bimap(f, g, this)
  private val bifunctor = object: Tuple2Bifunctor {}

  fun toPair(): Pair<A, B> = Pair(first, second)
}

// Monad
fun <S: Monoid<*, S>, A, B> Tuple2<S, A>.ap(type: Class<S>, f: Tuple2<S, (A) -> B>): Tuple2<S, B> = Tuple2.monad(type).ap(f, this)
fun <S: Monoid<*, S>, A, B> Tuple2<S, A>.bind(type: Class<S>, f: (A) -> Tuple2<S, B>): Tuple2<S, B> = Tuple2.monad(type).bind(f, this)

fun <A, B> Pair<A, B>.toTuple2(): Tuple2<A, B> = Tuple2(first, second)
