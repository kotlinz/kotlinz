package com.github.kotlinz.kotlinz.data.const

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid

data class Const<A, B>(val value: A): K2<Const.T, A, B> {
  class T

  companion object {
    fun <A, B> narrow(v: K1<K1<T, A>, B>) = v as Const<A, B>

    // Applicative
    fun <A: Monoid<*, A>, B> pure(type: Class<A>, v: B): Const<A, B> = applicative(type).pure(v)
    internal fun <A: Monoid<*, A>> applicative(type: Class<A>) = object: ConstApplicative<A> { override val type = type }

    // ApplicativeOps
    fun <S: Monoid<*, S>, A, B> liftA(type: Class<S>, f: (A) -> B) = applicativeOps(type).liftA(f)
    fun <S: Monoid<*, S>, A, B, C> liftA2(type: Class<S>, f: (A, B) -> C) = applicativeOps(type).liftA2(f)
    fun <S: Monoid<*, S>, A, B, C, D> liftA3(type: Class<S>, f: (A, B, C) -> D) = applicativeOps(type).liftA3(f)
    private fun <S: Monoid<*, S>> applicativeOps(type: Class<S>) = object: ConstApplicativeOps<S> { override val type = type }
  }

  // Functor
  infix fun <C> fmap(f: (B) -> C): Const<A, C> = functor.fmap(f, this)
  private val functor = object: ConstFunctor<A> {}

  // Contravariant
  infix fun <C> contramap(f: (C) -> B): Const<A, C> = contravariant.contramap(f, this)
  private val contravariant = object: ConstContravariant<A> {}

  fun <C> retag(): Const<A, C> = Const(value)
}

// Applicative
fun <S: Monoid<*, S>, A, B> Const<S, A>.ap(type: Class<S>, f: Const<S, (A) -> B>): Const<S, B> = Const.applicative(type).ap(f, this)
