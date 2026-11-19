package com.github.kotlinz.kotlinz.data.const

import com.github.kotlinz.kotlinz.K1
import com.github.kotlinz.kotlinz.data.either.Either
import com.github.kotlinz.kotlinz.K2
import com.github.kotlinz.kotlinz.type.group.Monoid

/**
 * A constant functor: holds a [value] of type A and ignores its phantom type parameter B.
 *
 * Use it to accumulate a summary while traversing (e.g. collecting with a Monoid), or as the getter
 * functor in lens-style code. Has instances for Functor and Contravariant (both just retag), and for
 * Applicative / ApplicativeOps when A is a Monoid (pass its `Class` to the helpers).
 *
 * ```kotlin
 * val type = StringMonoid::class.java
 * val add = Const.liftA2<StringMonoid, Int, Int, Int>(type) { a, b -> a + b }
 * add(Const(StringMonoid("a")), Const(StringMonoid("b"))) // Const(StringMonoid("ab"))
 * ```
 */
data class Const<A, B>(val value: A): K2<Const.T, A, B> {
  /** Type tag used for higher-kinded encoding. */
  class T

  companion object {
    /** Downcasts the higher-kinded representation back to [Const]. */
    fun <A, B> narrow(v: K1<K1<T, A>, B>) = v as Const<A, B>

    // Applicative
    /** Creates a Const holding the monoid identity of [type]. */
    fun <A: Monoid<*, A>, B> pure(type: Class<A>, v: B): Const<A, B> = applicative(type).pure(v)
    /** Applicative instance for the monoid [type]. */
    internal fun <A: Monoid<*, A>> applicative(type: Class<A>) = object: ConstApplicative<A> { override val type = type }

    // ApplicativeOps
    /** Lifts a unary function; the held value is unchanged. */
    fun <S: Monoid<*, S>, A, B> liftA(type: Class<S>, f: (A) -> B) = applicativeOps(type).liftA(f)
    /** Lifts a binary function; the held values are combined with the monoid. */
    fun <S: Monoid<*, S>, A, B, C> liftA2(type: Class<S>, f: (A, B) -> C) = applicativeOps(type).liftA2(f)
    /** Lifts a ternary function; the held values are combined with the monoid. */
    fun <S: Monoid<*, S>, A, B, C, D> liftA3(type: Class<S>, f: (A, B, C) -> D) = applicativeOps(type).liftA3(f)
    private fun <S: Monoid<*, S>> applicativeOps(type: Class<S>) = object: ConstApplicativeOps<S> { override val type = type }

    // Selective
    /** Combines the held values of [fab] and [fn] with the monoid, recording every possible effect. */
    fun <S: Monoid<*, S>, A, B> select(type: Class<S>, fab: K1<K1<T, S>, Either<A, B>>, fn: K1<K1<T, S>, (A) -> B>): Const<S, B> =
      object: ConstSelective<S> { override val type = type }.select(fab, fn)
  }

  // Functor
  /** Changes the phantom type; the held value is unchanged. */
  infix fun <C> fmap(f: (B) -> C): Const<A, C> = functor.fmap(f, this)
  private val functor = object: ConstFunctor<A> {}

  // Contravariant
  /** Changes the phantom type contravariantly; the held value is unchanged. */
  infix fun <C> contramap(f: (C) -> B): Const<A, C> = contravariant.contramap(f, this)
  private val contravariant = object: ConstContravariant<A> {}

  /** Changes the phantom type parameter. */
  fun <C> retag(): Const<A, C> = Const(value)
}

// Applicative
/** Combines the held value of [f] with this one using the monoid (`f.value <> this.value`). */
fun <S: Monoid<*, S>, A, B> Const<S, A>.ap(type: Class<S>, f: Const<S, (A) -> B>): Const<S, B> = Const.applicative(type).ap(f, this)
