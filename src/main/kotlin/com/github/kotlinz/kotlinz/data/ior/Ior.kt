package com.github.kotlinz.kotlinz.data.ior

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.syntax.MonadScope
import com.github.kotlinz.kotlinz.syntax.fx
import com.github.kotlinz.kotlinz.type.group.Semigroup

sealed class Ior<A, B>: K2<Ior.T, A, B> {
  class T

  data class Left<A, B>(val value: A): Ior<A, B>()
  data class Right<A, B>(val value: B): Ior<A, B>()
  data class Both<A, B>(val left: A, val right: B): Ior<A, B>()

  companion object {
    fun <A, B> narrow(v: K1<K1<T, A>, B>) = v as Ior<A, B>

    fun <A, B> left(v: A): Ior<A, B> = Left(v)
    fun <A, B> right(v: B): Ior<A, B> = Right(v)
    fun <A, B> both(a: A, b: B): Ior<A, B> = Both(a, b)

    // Monad
    fun <A: Semigroup<*, A>, B> pure(type: Class<A>, v: B): Ior<A, B> = monad(type).pure(v)
    fun <A: Semigroup<*, A>, B> join(type: Class<A>, v: K1<K1<T, A>, K1<K1<T, A>, B>>): Ior<A, B> = monad(type).join(v)
    internal fun <A: Semigroup<*, A>> monad(type: Class<A>) = object: IorMonad<A> { override val type = type }
    fun <A: Semigroup<*, A>, B> fx(type: Class<A>, block: suspend MonadScope<K1<T, A>>.() -> B): Ior<A, B> = narrow(monad(type).fx(block))

    // ApplicativeOps
    fun <S: Semigroup<*, S>, A, B> liftA(type: Class<S>, f: (A) -> B) = applicativeOps(type).liftA(f)
    fun <S: Semigroup<*, S>, A, B, C> liftA2(type: Class<S>, f: (A, B) -> C) = applicativeOps(type).liftA2(f)
    fun <S: Semigroup<*, S>, A, B, C, D> liftA3(type: Class<S>, f: (A, B, C) -> D) = applicativeOps(type).liftA3(f)
    private fun <S: Semigroup<*, S>> applicativeOps(type: Class<S>) = object: IorApplicativeOps<S> { override val type = type }

    // MonadOps
    fun <S: Semigroup<*, S>, A, B> liftM(type: Class<S>, f: (A) -> B) = monadOps(type).liftM(f)
    fun <S: Semigroup<*, S>, A, B, C> liftM2(type: Class<S>, f: (A, B) -> C) = monadOps(type).liftM2(f)
    fun <S: Semigroup<*, S>, A, B, C, D> liftM3(type: Class<S>, f: (A, B, C) -> D) = monadOps(type).liftM3(f)
    private fun <S: Semigroup<*, S>> monadOps(type: Class<S>) = object: IorMonadOps<S> { override val type = type }
  }

  // Functor
  infix fun <C> fmap(f: (B) -> C): Ior<A, C> = functor.fmap(f, this)
  private val functor = object: IorFunctor<A> {}

  // Bifunctor
  fun <C, D> bimap(f: (A) -> C, g: (B) -> D): Ior<C, D> = bifunctor.bimap(f, g, this)
  infix fun <C> first(f: (A) -> C): Ior<C, B> = bifunctor.first(f, this)
  infix fun <D> second(g: (B) -> D): Ior<A, D> = bifunctor.second(g, this)
  private val bifunctor = object: IorBifunctor {}
}

// Monad
fun <A: Semigroup<*, A>, B, C> Ior<A, B>.ap(type: Class<A>, f: Ior<A, (B) -> C>): Ior<A, C> = Ior.monad(type).ap(f, this)
fun <A: Semigroup<*, A>, B, C> Ior<A, B>.bind(type: Class<A>, f: (B) -> Ior<A, C>): Ior<A, C> = Ior.monad(type).bind(f, this)
